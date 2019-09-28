/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.protocol.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.phei.netty.protocol.netty.struct.NettyMessage;

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
public final class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

    private MarshallingEncoder marshallingEncoder;

    public NettyMessageEncoder() throws IOException {
        this.marshallingEncoder = new MarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf buf) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new Exception("The encode message is null");
        }
        buf.writeInt((msg.getHeader().getCrcCode()));
        buf.writeInt((msg.getHeader().getLength()));
        buf.writeLong((msg.getHeader().getSessionID()));
        buf.writeByte((msg.getHeader().getType()));
        buf.writeByte((msg.getHeader().getPriority()));
        buf.writeInt((msg.getHeader().getAttachment().size()));
        for (Map.Entry<String, Object> param : msg.getHeader().getAttachment().entrySet()) {
            byte[] keyArray = param.getKey().getBytes(StandardCharsets.UTF_8);
            buf.writeInt(keyArray.length);
            buf.writeBytes(keyArray);
            marshallingEncoder.encode(param.getValue(), buf);
        }
        if (msg.getBody() != null) {
            marshallingEncoder.encode(msg.getBody(), buf);
        } else {
            buf.writeInt(0);
        }
        buf.setInt(4, buf.readableBytes() - 8);
    }
}
