package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.env.Symbols;

import java.util.List;

public class ParameterList extends ASTList {
    private int[] offsets = null;

    public ParameterList(List<ASTree> c) {
        super(c);
    }

    public String name(int i) {
        return ((ASTLeaf) child(i)).token().getText();
    }

    public int size() {
        return numChildren();
    }

    @Override
    public void lookup(Symbols sym) {
        int s = size();
        offsets = new int[s];
        for (int i = 0; i < s; i++) {
            offsets[i] = sym.putNew(name(i));
        }
    }

    public void eval(Environment env, int index, Object value) {
        env.put(0, offsets[index], value);
    }
}
