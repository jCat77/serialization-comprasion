package com.jcat.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jcat.test.colfer.Event;
//import com.jcat.test.protobuf.EventOuterClass;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.msgpack.MessagePack;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class BenchRunner {

    private static final String EXAMPLE_ID = "testId1";
    private static final String EXAMPLE_USER_ID = "testUserId";
    private static final String EXAMPLE_PAYLOAD = "testPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayloadtestPayload";
    private static final Consumer<Object> BLAKCHOLE = (o) -> {};

    private static final ThreadLocal<BinaryEncoder> binaryEncoderForReuse = ThreadLocal.withInitial(() -> EncoderFactory.get().binaryEncoder(new ByteArrayOutputStream(0), null));
    private static final ThreadLocal<BinaryDecoder> binaryDecoderForReuse = ThreadLocal.withInitial(() -> DecoderFactory.get().binaryDecoder(new byte[0], null));
    private static final ThreadLocal<ByteArrayOutputStream> out = ThreadLocal.withInitial(ByteArrayOutputStream::new);

    private static final DatumWriter<com.jcat.test.avro.Event> userDatumWriter = new SpecificDatumWriter<>(com.jcat.test.avro.Event.class);
    private static final DatumReader<com.jcat.test.avro.Event> userDatumReader = new SpecificDatumReader<>(com.jcat.test.avro.Event.class);

    private static final ThreadLocal<MessagePack> messagePack = ThreadLocal.withInitial(() -> {
        final MessagePack messagePack = new MessagePack();
        messagePack.register(com.jcat.manual.Event.class);
        return messagePack;
    });

    private static final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(com.jcat.manual.Event.class);
        return kryo;
    });

    public static void main(String[] args) throws IOException, RunnerException {
//        Main.main(args);
        final BenchRunner benchRunner = new BenchRunner();
//        while (true) {
            benchRunner.testKryoImplementation();
//        }
    }

    @Benchmark
    //@Threads(5)
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 2, time = 5)
    @Fork(value = 1)
    public void testColferImplementation() {
        final Event event = new Event();
        event.setId(EXAMPLE_ID);
        event.setUserId(EXAMPLE_USER_ID);
        event.setPayload(EXAMPLE_PAYLOAD);
        event.setCreateDatetime(System.currentTimeMillis());

        byte[] serialized = new byte[900];
        event.marshal(serialized, 0);

        final Event event1 = new Event();
        event1.unmarshal(serialized, 0);

        BLAKCHOLE.accept(event1);
        BLAKCHOLE.accept(serialized);
    }

//    @Benchmark
//    //@Threads(5)
//    @Warmup(iterations = 1, time = 5)
//    @Measurement(iterations = 2, time = 5)
//    @Fork(value = 1)
//    public void testProtobufImplementation() throws InvalidProtocolBufferException {
//
//        final EventOuterClass.Event event = EventOuterClass.Event.newBuilder()
//                .setId(EXAMPLE_ID)
//                .setUserId(EXAMPLE_USER_ID)
//                .setPayload(EXAMPLE_PAYLOAD)
//                .setCreateTimestamp(System.currentTimeMillis())
//                .build();
//
//        final byte[] bytes = event.toByteArray();
//
//        final EventOuterClass.Event event1 = EventOuterClass.Event.parseFrom(bytes);
//
//        BLAKCHOLE.accept(event1);
//        BLAKCHOLE.accept(bytes);
//
//    }

    @Benchmark
    //@Threads(5)
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 2, time = 5)
    @Fork(value = 1)
    public void testAvroImplementation() throws IOException {
        final com.jcat.test.avro.Event event = new com.jcat.test.avro.Event(EXAMPLE_ID, EXAMPLE_USER_ID, EXAMPLE_PAYLOAD, System.currentTimeMillis());
        final ByteBuffer byteBuffer = event.toByteBuffer();
        final byte[] serialized = byteBuffer.array();

        final com.jcat.test.avro.Event event1 = com.jcat.test.avro.Event.fromByteBuffer(byteBuffer);

        BLAKCHOLE.accept(event1);
        BLAKCHOLE.accept(serialized);

    }


    @Benchmark
    //@Threads(5)
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 2, time = 5)
    @Fork(value = 1)
    public void testAvroAltImplementation() throws IOException {
        final com.jcat.test.avro.Event event = new com.jcat.test.avro.Event(EXAMPLE_ID, EXAMPLE_USER_ID, EXAMPLE_PAYLOAD, System.currentTimeMillis());

        final ByteArrayOutputStream byteArrayOutputStream = out.get();
        byteArrayOutputStream.reset();

        final BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, binaryEncoderForReuse.get());
        userDatumWriter.write(event, binaryEncoder);
        binaryEncoder.flush();
        final byte[] serialized = byteArrayOutputStream.toByteArray();

        final com.jcat.test.avro.Event event1 = userDatumReader.read(null, DecoderFactory.get().binaryDecoder(serialized, binaryDecoderForReuse.get()));

        BLAKCHOLE.accept(event1);
        BLAKCHOLE.accept(serialized);

    }

    @Benchmark
    //@Threads(5)
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 2, time = 5)
    @Fork(value = 1)
    public void testMsgpackImplementation() throws IOException {
        final com.jcat.manual.Event event = new com.jcat.manual.Event(EXAMPLE_ID, EXAMPLE_USER_ID, EXAMPLE_PAYLOAD, System.currentTimeMillis());

        final byte[] serialized = messagePack.get().write(event);

        final com.jcat.manual.Event event1 = messagePack.get().read(serialized, com.jcat.manual.Event.class);

        BLAKCHOLE.accept(event1);
        BLAKCHOLE.accept(serialized);

    }

    @Benchmark
    //@Threads(5)
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 2, time = 5)
    @Fork(value = 1)
    public void testKryoImplementation() throws IOException {
        final com.jcat.manual.Event event = new com.jcat.manual.Event(EXAMPLE_ID, EXAMPLE_USER_ID, EXAMPLE_PAYLOAD, System.currentTimeMillis());

        final ByteArrayOutputStream byteArrayOutputStream = out.get();
        byteArrayOutputStream.reset();

        Output output = new UnsafeOutput(byteArrayOutputStream);
        kryo.get().writeObject(output, event);

        byte[] serialized = output.getBuffer();

        Input input = new UnsafeInput(serialized);
        com.jcat.manual.Event event1 = kryo.get().readObject(input, com.jcat.manual.Event.class);

        BLAKCHOLE.accept(event1);
        BLAKCHOLE.accept(serialized);

    }
}
