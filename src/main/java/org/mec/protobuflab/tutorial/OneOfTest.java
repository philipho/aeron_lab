package org.mec.protobuflab.tutorial;

import org.mec.protobuflab.tutorial.protos.OneOfExampleProtos;

public class OneOfTest
{
    public static void main(String[] args)
    {
        OneOfExampleProtos.Example ex = OneOfExampleProtos.Example.newBuilder()
                .setMsgA(OneOfExampleProtos.SubMessageA.newBuilder()
                        .setFieldA("This is field A in Message A")
                        .build())
//                .setMsgB(OneOfExampleProtos.SubMessageB.newBuilder()
//                        .setFieldB("This is field B in Message B")
//                        .build())
                .build();

        switch (ex.getMyOneOfCase())
        {
            case MSG_A -> System.out.println("Message A is set: " + ex.getMsgA().getFieldA());
            case MSG_B -> System.out.println("Message B is set: " + ex.getMsgB().getFieldB());
            case MYONEOF_NOT_SET -> System.out.println("No message is set");
            default -> System.out.println("default: nothing is set");
        }
    }
}
