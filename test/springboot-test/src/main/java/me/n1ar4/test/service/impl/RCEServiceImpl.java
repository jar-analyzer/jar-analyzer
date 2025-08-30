package me.n1ar4.test.service.impl;

import me.n1ar4.test.service.RCEService;
import me.n1ar4.test.util.RunUtil;
import org.springframework.stereotype.Service;

@Service
public class RCEServiceImpl implements RCEService {
    @Override
    public String test(String cmd) {
        doInternal(cmd);
        return "ok";
    }

    private void doInternal(String cmd) {
        RunUtil.run(cmd);
    }
}
