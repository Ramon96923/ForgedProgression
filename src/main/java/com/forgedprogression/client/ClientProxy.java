package com.forgedprogression.client;

import com.forgedprogression.CommonProxy;

public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        ClientTextures.registerArmorTextures();
    }
}
