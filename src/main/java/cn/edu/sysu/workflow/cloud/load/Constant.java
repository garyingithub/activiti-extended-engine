package cn.edu.sysu.workflow.cloud.load;

import org.activiti.bpmn.converter.util.InputStreamProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constant {
    public static final long PERIOD = 60000;

    public static final int WATCHED_PERIOD_NUMBER = 50;
    public static final AtomicLong SERVER_ID_GENERATOR = new AtomicLong(0);
    public static final AtomicInteger INSTANCE_ID_GENERATOR = new AtomicInteger(0);

    public static final int BUFFER_SIZE = 1;
    public static final int HTTP_CONNECTIONS = 1500;
    public static final int TENANT_NUMBER = 3;
    public static final float[] TENANT_WEIGHTS = new float[] {0.2f, 0.3f, 0.5f};


    public static final int ENGINE_CAPACITY = 200;

    public static String ENGINE_ADDRESS = "119.29.61.136";
    public static String[] PORTS = new String[] {"8091", "8092"};

    public static void pastPeriod(int[] remainingCapacity, int capacity) {
        int[] newArray = new int[remainingCapacity.length];
        System.arraycopy(remainingCapacity, 1, newArray, 0, remainingCapacity.length - 1);
        newArray[remainingCapacity.length - 1] = capacity;
        remainingCapacity = newArray;
    }

    public static File getFileFromResource(String filePath) {
        URL filePathURL = Main.class.getClassLoader().getResource(filePath);
        if (filePathURL == null) {
            throw new RuntimeException("No such file");
        }
        return new File(filePathURL.getPath());
    }

    public static class FileInputStreamProvider implements InputStreamProvider {

        private File file;

        public FileInputStreamProvider(File file) {
            this.file = file;
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
