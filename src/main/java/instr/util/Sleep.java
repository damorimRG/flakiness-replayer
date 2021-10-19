package instr.util;


public class Sleep {

    public static synchronized void sleepIF(String methodName) {
        System.out.printf("sleeping... %s\n", methodName);
    }

    public static synchronized void report(String mensage) {
        System.out.println(mensage);
    }

}
