package jforgame.hotswap;

import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class provides a way to get bytes data and its className of a file.
 * It should be noted that the class uses some apis form tools.jar.
 */
 class ClassFileMeta {

    byte[] data;

    String className;

    public ClassFileMeta(File file) throws Exception {
        data = Files.readAllBytes(file.toPath());
        className = readClassName(data);
    }

    private String readClassName(byte[] data) throws IOException, ConstantPoolException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        return ClassFile.read(dis).getName().replaceAll("/", ".");
    }

    public byte[] getData() {
        return data;
    }

    public String getClassName() {
        return className;
    }
}
