package org.amapvox;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.amapvox.commons.Configuration;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Util;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.apache.log4j.Logger;

/**
 * AMAPVox main class that runs the simulations from command line arguments.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

    // valid command line arguments
    private final static String[] ARGS = new String[]{
        "-v", "--version", "-h", "--help", "--cfg=", "--T=", "--TT="
    };

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            // start amapvox gui
            Application.launch(org.amapvox.gui.MainFX.class, args);
        } else if (args[0].equals("viewer3d")) {
            // start amapvox viewer3d
            Application.launch(org.amapvox.viewer3d.MainFX.class, Arrays.copyOfRange(args, 1, args.length));
        } else {
            // amapvox command line
            for (String arg : args) {
                if (arg.equals("--help")) {
                    showHelp();
                    System.exit(0);
                }
                if (arg.equals("--version")) {
                    System.out.println("AMAPVox " + Util.getVersion());
                    System.exit(0);
                }
            }

            // args as list 
            List<String> largs = Arrays.asList(args);

            // check for unknown options
            List<String> invalidArgs = largs.stream()
                    // keep options
                    .filter(arg -> arg.startsWith("-"))
                    // identify unknown options
                    .filter(arg -> {
                        boolean ok = false;
                        for (String validArg : ARGS) {
                            ok |= arg.startsWith(validArg);
                        }
                        return !ok;
                    }).collect(Collectors.toList());

            if (!invalidArgs.isEmpty()) {
                LOGGER.error("Invalid command line arguments: " + Arrays.toString(invalidArgs.toArray()));
                showHelp();
                System.exit(1);
            }

            // run AMAPVox from command line
            new Main().run(largs);
        }
    }

    /**
     * Run AMAPVox without GUI from command line arguments.
     *
     * @param args, a list of command line arguments
     * @throws Exception
     */
    private void run(List<String> args) throws Exception {

        int nTreadTask = 1;
        int nThread = 1;

        // list of options (starting with --)
        List<String> options = args.stream()
                .filter(arg -> arg.startsWith("-"))
                .collect(Collectors.toList());

        // list of configurations (plain arguments)
        List<String> configurations = new ArrayList<>(args);
        configurations.removeAll(options);
        if (configurations.isEmpty()) {
            LOGGER.error("AMAPVox XML configuration missing");
            showHelp();
            System.exit(1);
        }

        // parse options
        for (String option : options) {

            // options must comply with format name=value(s)
            if (option.split("=").length != 2) {
                LOGGER.error("Invalid command line argument " + option);
                showHelp();
                System.exit(1);
            }

            // --T
            if (option.startsWith("--T=")) {
                nThread = Math.min(getNCpuFromArg(option), configurations.size());
            }

            // --TT
            if (option.startsWith("--TT=")) {
                nTreadTask = getNCpuFromArg(option);
            }
        }

        // create and init tasks
        List<AVoxTask> taskList = new ArrayList<>();
        for (String configuration : configurations) {
            taskList.add(provideTask(new File(configuration), nTreadTask));
        }
        for (AVoxTask task : taskList) {
            task.init();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Execute task(s) ");
        if (nThread > 1) {
            sb.append("concurrently (nThread=");
            sb.append(nThread);
            sb.append(")");
        } else {
            sb.append("sequentially");
        }
        LOGGER.info(sb.toString());

        // run task list
        ExecutorService exec = Executors.newFixedThreadPool(nThread);
        List<Future<File[]>> results = exec.invokeAll(taskList);

        // output summary
        LOGGER.info("OUTPUT SUMMARY");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");
        for (int itask = 0; itask < taskList.size(); itask++) {
            LOGGER.info(taskList.get(itask).getName() + " " + taskList.get(itask).getFile().getName());
            for (File file : results.get(itask).get()) {
                sb = new StringBuilder();
                sb.append("  (");
                sb.append(sdf.format(file.lastModified()));
                sb.append(", ");
                sb.append(Util.humanReadableByteCount(file.length(), true));
                sb.append(") ");
                sb.append(file.getAbsolutePath());
                LOGGER.info(sb.toString());
            }
        }

        // exit
        LOGGER.info("Simulation(s) completed successfully");
        System.exit(0);
    }

    /**
     * Returns a {@link org.amapvox.commons.AVoxTask} associated to the given
     * configuration file and allocates {@code ncpu} threads to the task.
     *
     * @param file, the XML configuration file.
     * @param ncpu, the number of threads allocated to the task
     * @return the {@link org.amapvox.commons.AVoxTask} associated to the
     * configuration file.
     * @throws Exception
     */
    private AVoxTask provideTask(File file, int ncpu) {

        try {
            Configuration cfg = Configuration.newInstance(file);
            return createTask(cfg.getTaskClass(), file, ncpu);
        } catch (Exception ex) {
            LOGGER.error("Unsupported/deprecated type of configuration file");
        }
        return null;
    }

    /**
     * Creates a custom {@link org.amapvox.commons.AVoxTask} based on the class
     * name, the configuration file and a number of allocated threads.
     *
     * @param className
     * @param file
     * @param ncpu
     * @return
     * @throws Exception
     */
    private AVoxTask createTask(Class<?> className, File file, int ncpu) throws Exception {

        Class<?>[] types = new Class[]{File.class, int.class};
        return (AVoxTask) className.getConstructor(types).newInstance(file, ncpu);
    }

    /**
     * Extract the number of threads from the command line argument. Expected
     * argument format --name=integer. For number of threads set to zero the
     * function returns maximum number of available processors.
     *
     * @param arg
     * @return
     */
    private static int getNCpuFromArg(String arg) {
        try {
            String[] nCpuArg = arg.split("=");
            return nCpuArg[1].equals("0")
                    ? Runtime.getRuntime().availableProcessors()
                    : Integer.parseInt(nCpuArg[1]);
        } catch (Exception ex) {
            LOGGER.error("Wrong format for argument " + arg);
            showHelp();
            System.exit(1);
        }
        return -1;
    }

    /**
     * Print help message in standard output.
     */
    public static void showHelp() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("AMAPVox. Run one or several configuration files, sequentially or concurrently.\n\n");
        sb.append("Usage:\n");
        sb.append("  amapvox [options] [file...] \n");
        sb.append("\n");
        sb.append("Options:\n");
        sb.append("  --help     ");
        sb.append("print help message.\n");
        sb.append("  --version  ");
        sb.append("print product version.\n");
        sb.append("  --T           ");
        sb.append("maximum number of threads for running tasks. By default T=1.\n");
        sb.append("                ");
        sb.append("  --T=4 run task list with as many as 4 threads.\n");
        sb.append("                ");
        sb.append("  --T=0 run task list with as many threads as possible.\n");
        sb.append("  --TT          ");
        sb.append("maximum number of threads per task. By default TT=1\n");
        sb.append("                ");
        sb.append("  --TT=8 run task with as many as 8 threads.\n");
        sb.append("                ");
        sb.append("  --TT=0 run task with as many threads as possible.\n");
        sb.append("\n");
        sb.append("Examples:\n");
        sb.append("  # sequential execution, monothread configurations\n");
        sb.append("  amapvox /home/file1.xml /home/file2.xml /home/file3.xml\n");
        sb.append("  # concurrent execution, monothread configurations\n");
        sb.append("  amapvox --T=3 /home/file1.xml /home/file2.xml /home/file3.xml\n");
        sb.append("  # sequential execution, multithread configuration\n");
        sb.append("  amapvox --TT=2 /home/file1.xml\n");
        sb.append("  # concurrent execution, multithread configurations (total 4 CPU)\n");
        sb.append("  amapvox --T=2 --TT=2 /home/file1.xml /home/file2.xml\n");

        System.out.println(sb.toString());
        org.amapvox.viewer3d.MainFX.showHelp();
    }

}
