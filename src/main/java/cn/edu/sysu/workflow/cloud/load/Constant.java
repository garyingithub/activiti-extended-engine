package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.data.ProcessInstance;
import org.activiti.bpmn.converter.util.InputStreamProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constant {
    public static final Random RANDOM = new Random();
    public static final long PERIOD = 60000;

    public static final int TOTAL = 40000;

    public static final int WATCHED_PERIOD_NUMBER = 500;
    public static final AtomicLong SERVER_ID_GENERATOR = new AtomicLong(0);
    public static final AtomicInteger INSTANCE_ID_GENERATOR = new AtomicInteger(0);

    public static final int BUFFER_SIZE = 1;
    public static final int HTTP_CONNECTIONS = 1000;
    public static final int TENANT_NUMBER = 10;
    public static final float[] TENANT_WEIGHTS = new float[] {0.5f, 0.5f,0.5f, 0.5f,0.5f};


    public static final int ENGINE_CAPACITY = 500;
    public static final int GRAPH_WATCHED_NUMBER = 20;
    public static final int INPUT_INSTANCE_NUMBER = 500;

    public static String ENGINE_ADDRESS = "119.29.61.136";
    public static String[] PORTS = new String[] {"8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092", "8091", "8092"};

    public static int ENGINE_NUMBER = 50;
    public static void pastPeriod(int[] remainingCapacity, int capacity) {

        for(int i = 0; i < remainingCapacity.length - 1; i++) {
            remainingCapacity[i] = remainingCapacity[i + 1];
        }
        remainingCapacity[remainingCapacity.length - 1] = capacity;
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

    public static int getTenantId(ProcessInstance instance) {
//       return instance.getDefinitionId().equals("oop") ? 1 : 0;
       return instance.getId() % Constant.TENANT_NUMBER;// < 1 ? 0 : 1;
    }

    public static boolean bigger(int[] a, int[] b) {
        for(int i = 0; i < a.length && i < b.length; i++) {
            if(a[i] < b[i]) {
                return false;
            }
            return true;
        }
        return false;
    }
}
