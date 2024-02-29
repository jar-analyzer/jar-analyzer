package org.vidar.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhchen
 */
@Data
public class GraphCall {
    private final MethodReference.Handle method;
    private List<MethodReference.Handle> callMethods = new ArrayList<>();

    public GraphCall(MethodReference.Handle method) {
        this.method = method;
    }

    public void addCallMethod(MethodReference.Handle handle) {
        this.callMethods.add(handle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GraphCall graphCall = (GraphCall) o;
        return method.equals(graphCall.method);
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    public static class Factory implements DataFactory<GraphCall> {

        @Override
        public GraphCall parse(String[] fields) {
            GraphCall graphCall = new GraphCall(new MethodReference.Handle(new ClassReference.Handle(fields[0]), fields[1], fields[2]));
            for (int i = 3; i < fields.length; i+=3) {
                MethodReference.Handle handle = new MethodReference.Handle(new ClassReference.Handle(fields[i]), fields[i + 1], fields[i + 2]);
                graphCall.addCallMethod(handle);
            }
            return graphCall;
        }

        @Override
        public String[] serialize(GraphCall obj) {
            ArrayList<String> res = new ArrayList<>();
            res.add(obj.method.getClassReference().getName());
            res.add(obj.method.getName());
            res.add(obj.method.getDesc());

            obj.callMethods.forEach(m -> {
                res.add(m.getClassReference().getName());
                res.add(m.getName());
                res.add(m.getDesc());
            });
            return res.toArray(new String[0]);
        }
    }
}
