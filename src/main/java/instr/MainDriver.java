package instr;

/* Usage: java MainDriver [soot-options] appClass
*/
/* import necessary soot pppackages */
import instr.transformers.AddDelaysTransformer;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import soot.PackManager;
import soot.Transform;
import soot.Transformer;
import soot.options.Options;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import soot.Pack;

import static java.lang.System.exit;

public class MainDriver {

    public static void main(String[] args) {
        /* check the arguments */
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption(new Option("o", "overHead", true, "[REQUIRED] overHead of time: 0.1 - 1"));
        options.addOption(new Option("m", "minimalDelay", true, "[REQUIRED] Turn on extract: Number in milisseconds"));
//        options.addOption(new Option("csv", "csvTimes", true, "[REQUIRED] csv file with tests names and time."));
        options.addOption(new Option("r", "randomSeed", true, "[REQUIRED] Seed of Random()."));
        options.addOption(new Option("d", "debugger", false, "Enable debugger mode"));
        CommandLineParser parser = new DefaultParser();
        String[] classNames = null;
        double overhead = 0;
        long minimalDelay = 0, randomSeed = 0;
        String csvTimes = "";
        boolean debugger = false;

        try {
            CommandLine cmd = parser.parse(options, args);
            if (! (cmd.hasOption("o") && cmd.hasOption("m") && cmd.hasOption("r"))){
                throw new Exception("Erro, without the required arguments");
            }
//            String className = cmd.getOptionValue( "className");
            overhead = Double.parseDouble(cmd.getOptionValue("overHead"));
            minimalDelay = Long.parseLong(cmd.getOptionValue("minimalDelay"));
//            csvTimes = cmd.getOptionValue("csvTimes" );
//            classNames = loadFromFile(csvTimes);
            // debugging --->
            classNames = new String[] {
                    "com.alibaba.json.bvt.parser.TypeUtilsTest_interface",
                    "com.alibaba.json.bvt.parser.autoType.AutoTypeTest2_deny"};
            randomSeed = Long.parseLong(cmd.getOptionValue("randomSeed"));
            if (cmd.hasOption("d")){
                debugger = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error parsing command-line arguments!");
            System.out.println("Please, follow the instructions below:");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "...", options );
            exit(1);
        }

        // Set Soot's internal classpath
        String javapath = System.getProperty("java.class.path");
        String jredir = System.getProperty("java.home")+"/lib/rt.jar";
        String path = javapath+File.pathSeparator+jredir;


        // debugging (see "for debugging")
        try(FileInputStream inputStream = new FileInputStream("/tmp/target.classpath")) {
            path = path + File.pathSeparator + IOUtils.toString(inputStream);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        Options.v().set_soot_classpath(path);

        /* add a phase to transformer pack by call Pack.add */
        Pack jtp = PackManager.v().getPack("jtp");
        // new PrintFixedMethodTransformer();
        // new InvokeStaticTransformer();
        // new SleepTransformer();
        // new SleepInvokerTransformer();
        Transformer transformer = new AddDelaysTransformer(overhead, minimalDelay, randomSeed, debugger);
        jtp.add(new Transform("jtp.instrumenter", transformer));

        /* Give control to Soot to process all options,
        * InvokeStaticInstrumenter.internalTransform will get called.
        */
        soot.Main.main(classNames);
    }

    public static String[] loadFromFile(String csvFile){
        Set<String> classNames = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                String completeName = line.split(",")[0];
                String name = completeName.split("#")[0];
                if (!name.startsWith("FAILURE")) {
                    classNames.add(name);
                }
            }
        } catch (IOException ioException) {
            System.err.print("fatal error! csv file can't be found " + csvFile);
            System.exit(1);
        }
        return classNames.toArray(new String[classNames.size()]);
    }
}