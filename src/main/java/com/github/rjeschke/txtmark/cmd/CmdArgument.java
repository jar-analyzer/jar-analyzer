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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for command line parsing.
 *
 * This is a copy from {@link https://github.com/rjeschke/neetutils-base}.
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface CmdArgument
{
    /**
     * Long name for argument. Default is 'none'. Either one or both of
     * {@code l}, {@code s} need to be provided.
     */
    String l() default "";

    /**
     * Short name (character) for argument. Default is 'none'. Either one or
     * both of {@code l}, {@code s} need to be provided.
     */
    char s() default '\0';

    /**
     * A description for this argument. Default is 'none'.
     */
    String desc() default "";

    /**
     * List item separator. Default is ','.
     */
    char listSep() default ',';

    /**
     * Class for List-type arguments. Default is {@code String.class}.
     */
    Class<?> listType() default String.class;

    /**
     * Set to {@code true} if this is a switch. Requires a {@code boolean} field
     * which gets set to {@code true} when this argument is provided.
     */
    boolean isSwitch() default false;

    /**
     * Set to {@code true} if this is a required argument.
     */
    boolean required() default false;

    /**
     * Set to {@code true} to set this as a catch-all argument. Requires a
     * {@code List} field and will parse all arguments following this switch
     * into the list.
     */
    boolean catchAll() default false;

    /**
     * Set to {@code false} to disable automatic default value printing for this
     * argument.
     */
    boolean printDefault() default true;
}
