/*
 * Copyright (C) 2013-2015 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rjeschke.txtmark.cmd;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Generic command line parser.
 *
 * This is a copy from {@link https://github.com/rjeschke/neetutils-base}.
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 */
final class CmdLineParser
{
    private CmdLineParser()
    {
        // meh!
    }

    enum Type
    {
        UNSUPPORTED, STRING, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, LIST, BOOL;
    }

    final static HashMap<Class<?>, Type> TYPE_MAP        = new HashMap<Class<?>, Type>();
    final static Class<?>[]              TYPE_CLASS_LIST = Colls.<Class<?>> objArray(String.class, byte.class,
                                                                 Byte.class, short.class, Short.class, int.class,
                                                                 Integer.class, long.class, Long.class, float.class,
                                                                 Float.class, double.class, Double.class, List.class,
                                                                 Boolean.class, boolean.class);
    final static Type[]                  TYPE_TYPE_LIST  = Colls.objArray(Type.STRING, Type.BYTE, Type.BYTE,
                                                                 Type.SHORT,
                                                                 Type.SHORT, Type.INT, Type.INT, Type.LONG, Type.LONG,
                                                                 Type.FLOAT, Type.FLOAT, Type.DOUBLE, Type.DOUBLE,
                                                                 Type.LIST, Type.BOOL, Type.BOOL);

    final static HashSet<String>         BOOL_TRUE       = new HashSet<String>(Colls.list("on", "true", "yes"));
    final static HashSet<String>         BOOL_FALSE      = new HashSet<String>(Colls.list("off", "false", "no"));

    static
    {
        for (int i = 0; i < TYPE_CLASS_LIST.length; i++)
        {
            TYPE_MAP.put(TYPE_CLASS_LIST[i], TYPE_TYPE_LIST[i]);
        }
    }

    static Type getTypeFor(final Class<?> clazz)
    {
        final Type type = TYPE_MAP.get(clazz);

        if (type != null)
        {
            return type;
        }

        if (Classes.implementsInterface(clazz, List.class))
        {
            return Type.LIST;
        }

        return Type.UNSUPPORTED;
    }

    static String defaultToString(final Object value, final Type type, final Arg arg)
    {
        if (value == null || arg.isSwitch || arg.catchAll || !arg.printDefault)
        {
            return null;
        }

        if (type == Type.LIST)
        {
            final List<?> list = (List<?>)value;

            if (list.isEmpty())
            {
                return null;
            }

            final StringBuilder sb = new StringBuilder();
            final Once<String> once = Once.of("", Character.toString(arg.itemSep));
            for (final Object o : list)
            {
                sb.append(once.get());
                sb.append(o.toString());
            }
            return sb.toString();
        }

        return value.toString();
    }

    private static void parseArgs(final Object[] objs, final List<Arg> allArgs, final HashMap<String, Arg> shortArgs,
            final HashMap<String, Arg> longArgs)
            throws IOException
    {
        for (final Object obj : objs)
        {
            final Class<?> cl = obj.getClass();
            final Field[] fields = cl.getDeclaredFields();

            for (final Field f : fields)
            {
                if (f.isAnnotationPresent(CmdArgument.class))
                {
                    final Arg arg = new Arg(f.getAnnotation(CmdArgument.class), obj, f);

                    if (arg.type == Type.UNSUPPORTED)
                    {
                        throw new IOException("Unsupported parameter type: " + f.getType().getCanonicalName()
                                + " for: " + arg);
                    }

                    if (arg.listType == Type.UNSUPPORTED || arg.listType == Type.LIST)
                    {
                        throw new IOException("Unsupported list type: " + f.getType().getCanonicalName() + " for: "
                                + arg);
                    }

                    if (Strings.isEmpty(arg.s) && Strings.isEmpty(arg.l))
                    {
                        throw new IOException("Missing parameter name");
                    }

                    if (!Strings.isEmpty(arg.s))
                    {
                        if (shortArgs.containsKey(arg.s))
                        {
                            throw new IOException("Duplicate short argument: -" + arg.s);
                        }
                        shortArgs.put(arg.s, arg);
                    }

                    if (!Strings.isEmpty(arg.l))
                    {
                        if (longArgs.containsKey(arg.l))
                        {
                            throw new IOException("Duplicate long argument: --" + arg.l);
                        }
                        longArgs.put(arg.l, arg);
                    }

                    if (arg.isCatchAll() && arg.type != Type.LIST)
                    {
                        throw new IOException("Parameter '" + arg + "' requires a List field.");
                    }

                    if (arg.isSwitch && arg.type != Type.BOOL)
                    {
                        throw new IOException("Parameter '" + arg + "' requires a Boolean/boolean field.");
                    }

                    allArgs.add(arg);
                }
            }
        }
    }

    /**
     * Generates a formatted help (Unix-style) for the given argument objects.
     *
     * @param columnWidth
     *            Maximum column width. Words get wrapped at spaces.
     * @param sort
     *            Set {@code true} to sort arguments before printing.
     * @param objs
     *            One or more objects with annotated public fields.
     * @return The formatted argument help text.
     * @throws IOException
     *             if a parsing error occurred.
     * @see CmdArgument
     */
    public static String generateHelp(final int columnWidth, final boolean sort, final Object... objs)
            throws IOException
    {
        final List<Arg> allArgs = Colls.list();
        final HashMap<String, Arg> shortArgs = new HashMap<String, Arg>();
        final HashMap<String, Arg> longArgs = new HashMap<String, Arg>();

        parseArgs(objs, allArgs, shortArgs, longArgs);

        int minArgLen = 0;

        for (final Arg a : allArgs)
        {
            int len = a.toString().length();
            if (!a.isSwitch)
            {
                ++len;
                len += a.getResolvedType().toString().length();
                if (a.isCatchAll())
                {
                    ++len;
                }
                else if (a.isList())
                {
                    len += 6;
                }
            }
            minArgLen = Math.max(minArgLen, len);
        }
        minArgLen += 2;
        if (sort)
        {
            Collections.sort(allArgs);
        }

        final StringBuilder sb = new StringBuilder();

        for (final Arg a : allArgs)
        {
            final StringBuilder line = new StringBuilder();
            line.append(' ');
            line.append(a);
            if (!a.isSwitch)
            {
                line.append(' ');
                line.append(a.getResolvedType().toString().toLowerCase());
                if (a.isCatchAll())
                {
                    line.append('s');
                }
                else if (a.isList())
                {
                    line.append('[');
                    line.append(a.itemSep);
                    line.append("...]");
                }
            }
            while (line.length() < minArgLen)
            {
                line.append(' ');
            }

            line.append(':');

            final StringBuilder desc = new StringBuilder(a.desc.trim());

            final String defVal = defaultToString(a.safeFieldGet(), a.type, a);

            if (defVal != null)
            {
                desc.append(" Default is: '");
                desc.append(defVal);
                desc.append("'.");
            }

            final List<String> toks = Strings.split(desc.toString(), ' ');

            for (final String s : toks)
            {
                if (line.length() + s.length() + 1 > columnWidth)
                {
                    sb.append(line);
                    sb.append('\n');
                    line.setLength(0);
                    while (line.length() <= minArgLen)
                    {
                        line.append(' ');
                    }
                    line.append(' ');
                }
                line.append(' ');
                line.append(s);
            }

            if (line.length() > minArgLen)
            {
                sb.append(line);
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    /**
     * Parses command line arguments.
     *
     * @param args
     *            Array of arguments, like the ones provided by
     *            {@code void main(String[] args)}
     * @param objs
     *            One or more objects with annotated public fields.
     * @return A {@code List} containing all unparsed arguments (i.e. arguments
     *         that are no switches)
     * @throws IOException
     *             if a parsing error occurred.
     * @see CmdArgument
     */
    public static List<String> parse(final String[] args, final Object... objs) throws IOException
    {
        final List<String> ret = Colls.list();

        final List<Arg> allArgs = Colls.list();
        final HashMap<String, Arg> shortArgs = new HashMap<String, Arg>();
        final HashMap<String, Arg> longArgs = new HashMap<String, Arg>();

        parseArgs(objs, allArgs, shortArgs, longArgs);

        for (int i = 0; i < args.length; i++)
        {
            final String s = args[i];

            final Arg a;

            if (s.startsWith("--"))
            {
                a = longArgs.get(s.substring(2));
                if (a == null)
                {
                    throw new IOException("Unknown switch: " + s);
                }
            }
            else if (s.startsWith("-"))
            {
                a = shortArgs.get(s.substring(1));
                if (a == null)
                {
                    throw new IOException("Unknown switch: " + s);
                }
            }
            else
            {
                a = null;
                ret.add(s);
            }

            if (a != null)
            {
                if (a.isSwitch)
                {
                    a.setField("true");
                }
                else
                {
                    if (i + 1 >= args.length)
                    {
                        System.out.println("Missing parameter for: " + s);
                    }
                    if (a.isCatchAll())
                    {
                        final List<String> ca = Colls.list();
                        for (++i; i < args.length; ++i)
                        {
                            ca.add(args[i]);
                        }
                        a.setCatchAll(ca);
                    }
                    else
                    {
                        ++i;
                        a.setField(args[i]);
                    }
                }
                a.setPresent();
            }
        }

        for (final Arg a : allArgs)
        {
            if (!a.isOk())
            {
                throw new IOException("Missing mandatory argument: " + a);
            }
        }

        return ret;
    }

    private static class Arg implements Comparable<Arg>
    {
        final String  s;
        final String  l;
        final String  id;
        final String  desc;
        final char    itemSep;
        final boolean isSwitch;
        final boolean required;
        final boolean catchAll;
        final boolean printDefault;
        final Type    type;
        final Type    listType;
        boolean       present = false;
        final Object  object;
        final Field   field;

        public Arg(final CmdArgument arg, final Object obj, final Field field)
        {
            this.s = arg.s() == 0 ? "" : Character.toString(arg.s());
            this.l = arg.l();
            this.desc = arg.desc();
            this.isSwitch = arg.isSwitch();
            this.required = arg.required();
            this.catchAll = arg.catchAll();
            this.itemSep = arg.listSep();
            this.printDefault = arg.printDefault();
            this.id = this.s + "/" + this.l;

            this.object = obj;
            this.field = field;
            this.type = getTypeFor(this.field.getType());
            this.listType = getTypeFor(arg.listType());
        }

        public Type getResolvedType()
        {
            return this.isList() ? this.listType : this.type;
        }

        public boolean isCatchAll()
        {
            return this.catchAll;
        }

        public boolean isList()
        {
            return this.type == Type.LIST;
        }

        public void setCatchAll(final List<String> list) throws IOException
        {
            this.setListField(list);
        }

        public void setListField(final List<String> list) throws IOException
        {
            try
            {
                if (this.listType == Type.STRING)
                {
                    this.field.set(this.object, list);
                }
                else
                {
                    final List<Object> temp = Colls.list();
                    for (final String i : list)
                    {
                        temp.add(this.toObject(i, this.listType));
                    }
                    this.field.set(this.object, temp);
                }
            }
            catch (final IllegalArgumentException e)
            {
                throw new IOException("Failed to write value", e);
            }
            catch (final IllegalAccessException e)
            {
                throw new IOException("Failed to write value", e);
            }
        }

        Object safeFieldGet()
        {
            try
            {
                return this.field.get(this.object);
            }
            catch (final Exception e)
            {
                return null;
            }
        }

        private Object toObject(final String value, final Type type) throws IOException
        {
            try
            {
                switch (type)
                {
                case STRING:
                    return value;
                case BYTE:
                    return Byte.parseByte(value);
                case SHORT:
                    return Short.parseShort(value);
                case INT:
                    return Integer.parseInt(value);
                case LONG:
                    return Long.parseLong(value);
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case BOOL:
                    if (BOOL_TRUE.contains(value.toLowerCase()))
                    {
                        return true;
                    }
                    if (BOOL_FALSE.contains(value.toLowerCase()))
                    {
                        return false;
                    }
                    throw new IOException("Illegal bool value for:" + this.toString());
                default:
                    throw new IOException("Illegal type: " + type.toString().toLowerCase());
                }
            }
            catch (final Throwable t)
            {
                throw new IOException("Parsing error for: " + this.toString() + "; '" + value + "'", t);
            }
        }

        public void setField(final String value) throws IOException
        {
            try
            {
                if (this.isList())
                {
                    this.setListField(Strings.split(value, this.itemSep));
                }
                else
                {
                    this.field.set(this.object, this.toObject(value, this.type));
                }
            }
            catch (final IllegalArgumentException e)
            {
                throw new IOException("Failed to write field: " + this.field.getName(), e);
            }
            catch (final IllegalAccessException e)
            {
                throw new IOException("Failed to write field: " + this.field.getName(), e);
            }
        }

        public void setPresent()
        {
            this.present = true;
        }

        public boolean isOk()
        {
            return !this.required || this.present;
        }

        @Override
        public int hashCode()
        {
            return this.id.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Arg)
            {
                return this.id.equals(((Arg)obj).id);
            }
            return false;
        }

        @Override
        public String toString()
        {
            if (Strings.isEmpty(this.s))
            {
                return "    --" + this.l;
            }
            if (Strings.isEmpty(this.l))
            {
                return "-" + this.s;
            }
            return "-" + this.s + ", --" + this.l;
        }

        @Override
        public int compareTo(final Arg o)
        {
            final String a = Strings.isEmpty(this.s) ? this.l : this.s;
            final String b = Strings.isEmpty(o.s) ? o.l : o.s;
            return a.compareTo(b);
        }
    }

    private static class Once<T>
    {
        private final T first;
        private final T next;
        private boolean isFirst = true;

        public Once(final T first, final T next)
        {
            this.first = first;
            this.next = next;
        }

        public static <T> Once<T> of(final T first, final T next)
        {
            return new Once<T>(first, next);
        }

        public T get()
        {
            if (this.isFirst)
            {
                this.isFirst = false;
                return this.first;
            }

            return this.next;
        }
    }

    private final static class Colls
    {
        final static <T> T[] objArray(final T... ts)
        {
            return ts;
        }

        final static <A> List<A> list(final A... coll)
        {
            final List<A> ret = new ArrayList<A>(coll.length);
            for (int i = 0; i < coll.length; i++)
            {
                ret.add(coll[i]);
            }
            return ret;
        }
    }

    private final static class Classes
    {
        final static boolean implementsInterface(final Class<?> clazz, final Class<?> interfce)
        {
            for (final Class<?> c : clazz.getInterfaces())
            {
                if (c.equals(interfce))
                {
                    return true;
                }
            }

            return false;
        }
    }

    private final static class Strings
    {
        public final static boolean isEmpty(final String str)
        {
            return str == null || str.isEmpty();
        }

        public final static List<String> split(final String str, final char ch)
        {
            final List<String> ret = Colls.list();

            if (str != null)
            {
                int s = 0, e = 0;
                while (e < str.length())
                {
                    if (str.charAt(e) == ch)
                    {
                        ret.add(str.substring(s, e));
                        s = e + 1;
                    }
                    e++;
                }
                ret.add(str.substring(s, e));
            }

            return ret;
        }
    }
}
