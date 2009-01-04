package org.genedb.pipeline.infrastructure;

import java.io.File;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandException;
import org.springframework.batch.core.step.tasklet.SystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;



public class LsfSubmitter extends StepExecutionListenerSupport implements Tasklet {
	
    private static final Logger logger = Logger.getLogger(LsfSubmitter.class);

    // LSF Commands
    private final static String SUBMISSION = "bsub";
    private final static String LIST = "bjobs";
    private final static String KILL = "bkill";
	private static final File DEFAULT_LSF_BINDIR = null;
    
    // LSF Parameters
    private File lsfBinDirectory = DEFAULT_LSF_BINDIR;;
    public static final String DEFAULT_QUEUE_NAME = "normal";
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected String queueName = DEFAULT_QUEUE_NAME;
    
    protected String hostList;
    protected String processor = DEFAULT_PROCESSOR_NUMBER;
    protected String interactive = "false";
    protected String resourceRequirement = "";    
    
    // LSF Runtime Details
    protected int jobID;
    private String hostName;
    
    
    
    protected String jobname;
    protected String commandPath;
    protected String[] environment;

    private boolean isStarted;

    /**
     * Builds bkill command and encapsulates it in a process
     * @param jobID The id of the job previously launched
     * @return ExternalProcess The process encapsulating the bkill command
     */
    public int buildBKillProcess() {
    	// TODO Run as synch task
    	String cmd = KILL + " " + jobID;
        return -1;
    }

    
    /**
     * Returns the id of the job associated to this process
     * @return int
     */
    public int getJobID() {
        return jobID;
    }

    /**
     * Returns the name of the queue where the job was launched
     * @return String
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the value of the queue where the job will be launched. The default is 'normal'
     * @param queueName
     */
    public void setQueueName(String queueName) {
        checkStarted();
        if (queueName == null) {
            throw new NullPointerException();
        }
        this.queueName = queueName;
    }

    /**
     * Sets the value of the hostList parameter with the given value
     * @param hostList
     */
    public void setHostList(String hostList) {
        checkStarted();
        this.hostList = hostList;
    }

    /**
     * Returns the hostList value of this process.
     * @return String
     */
    public String getHostList() {
        return hostList;
    }

    /**
     * Returns true if this BsubProcess is lauched with -I option false otherwise
     * @return boolean
     */
    public String isInteractive() {
        return interactive;
    }

    /**
     * Allows to launch this BsubProcess with -I (interactive option)
     * @param interactive true for -I option false otherwise
     */
    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    /**
     * Sets the number of processor requested when running the job
     * @param processor
     */
    public void setProcessorNumber(String processor) {
        checkStarted();
        if (processor != null) {
            this.processor = processor;
        }
    }

//    public String getProcessId() {
//        return "lsf_" + targetTasklet.getProcessId();
//    }

    public int getNodeNumber() {
        return (new Integer(getProcessorNumber()).intValue());
    }


    /**
     * Returns the number of processor requested for the job
     * @return String
     */
    public String getProcessorNumber() {
        return processor;
    }

    public String getResourceRequirement() {
        return resourceRequirement;
    }

    public void setResourceRequirement(String resourceRequirement) {
        this.resourceRequirement = "-R " + resourceRequirement + " ";
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    protected String buildBSubCommand() {
        StringBuilder bSubCommand = new StringBuilder();
        bSubCommand.append(commandPath);
        if (interactive.equals("true")) {
            bSubCommand.append(" -I");
        }
        bSubCommand.append(" -n " + processor + " -q " + queueName + " ");
        if (hostList != null) {
            bSubCommand.append("-m " + hostList + " ");
        }
        if (jobname != null) {
            bSubCommand.append("-J " + jobname + " ");
        }

        bSubCommand.append(getResourceRequirement() + " " + command);

        return bSubCommand.toString();
    }

    protected String buildBJobsCommand() {
        return lsfBinDirectory + " " + jobID;
    }

    /**
     * parses a message in order to find the job id of the
     * launched job.
     * we assume here that the jobid is displayed following this
     * convention :
     *    Job <...>
     */
    protected int parseJobID(String message) {
    	logger.debug("parseJobID analyzing " + message);
        String beginJobIDMarkup = "Job <";
        String endJobIDMarkup = ">";
        int n1 = message.indexOf(beginJobIDMarkup);
        if (n1 == -1) {
            return 0;
        }
        int n2 = message.indexOf(endJobIDMarkup, n1 + beginJobIDMarkup.length());
        if (n2 == -1) {
            return 0;
        }
        String id = message.substring(n1 + beginJobIDMarkup.length(), n2);
        logger.debug("!!!!!!!!!!!!!! JOBID = " + id);
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * parses the hostname from a string. We assume that the line
     * looks like that :
     *    191009 user  status  queue     fromHost     targetHost        *eep 10000 Jan 25 13:33
     * Where targetHost is the hostname we are looking for.
     * status could be at least
     *     - PEND for pending (means targethost is undetermined
     *     - anything else (means targethost is known
     * @param message the string that may contains the hostname
     * @return null if the message did not contains any hostname,
     * an empty string if the message did contains the target host but
     * was undertermined because the job was still pending. Return the
     * hostname if it is found.
     */
    protected String parseHostname(String message) {
    	logger.debug("parseHostname analyzing " + message);
        java.util.StringTokenizer st = new java.util.StringTokenizer(message);
        if (st.countTokens() < 6) {
            return null; // we expect at least 6 tokens
        }
        try {
            int currentJobID = Integer.parseInt(st.nextToken());
            if (currentJobID != jobID) {
                return null; // not the same id
            }
        } catch (NumberFormatException e) {
            return null;
        }
        st.nextToken(); // ignore user
        String status = st.nextToken();
        if (status.equals("PEND")) {
            return ""; // not running yet
        }
        st.nextToken(); // ignore queue
        st.nextToken(); // ignore fromHost
        String hostname = st.nextToken();
        logger.debug("!!!!!!!!!!!!!! hostname = " + hostname);
        logger.info("token " + st.countTokens());
        return hostname;
    }

    protected void checkStarted() {
        if (isStarted) {
            throw new IllegalStateException("Process already started");
        }
    }

    /**
     * Implementation of a RemoteProcessMessageLogger that look for the jobID of the launched job
     */
    public class LsfOutputParser {
        private boolean foundJobID;
        private boolean foundHostname;

        public void log(String message) {
            //int nbProcessor = (new Integer(processor)).intValue();
            //parseHostname(message);
            if (!foundJobID) {
                jobID = parseJobID(message);
                foundJobID = jobID != 0;
//                if (foundJobID) {
//                    sendJobDetailsCommand();
//                }
            } else if (!foundHostname) {
                hostName = parseHostname(message);
                if (hostName != null) {
                    //int counter=1;
                    foundHostname = hostName.length() > 0;
                    //while(counter < nbProcessor){
                    //parseHostname(message);
                    //counter ++;
                    //}
                    if (foundHostname) {
                        // we are done
                        //outputMessageSink.setMessage(null);
                    } else {
                        // send another command to fetch the hostname
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        //sendJobDetailsCommand();
                    }
                }
            }
        }
    }

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

	private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

	protected String[] environmentParams;

	protected File workingDirectory;

	private boolean interruptOnCancel;
    
	@Override
	public RepeatStatus execute(StepContribution contribution,
			AttributeAccessor attributes) throws Exception {
		
		FutureTask<Integer> systemCommandTask = new FutureTask<Integer>(new Callable<Integer>() {

			public Integer call() throws Exception {
				Process process = Runtime.getRuntime().exec(lsfCommand, environmentParams, workingDirectory);
				return process.waitFor();
			}

		});

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		taskExecutor.execute(systemCommandTask);

		try {
			while (true) {
				Thread.sleep(checkInterval);
				if (systemCommandTask.isDone()) {
					return RepeatStatus.FINISHED;
				} else {
					if (stopWatch.getTime() > timeout) {
						systemCommandTask.cancel(interruptOnCancel);
						throw new SystemCommandException("Execution of system command did not finish within the timeout");
					} else {
						if (execution.isTerminateOnly()) {
							systemCommandTask.cancel(interruptOnCancel);
							throw new JobInterruptedException("Job interrupted while executing system command '" + lsfCommand
									+ "'");
						}
					}
				}
			}
		}
		finally {
			stopWatch.stop();
		}
	}
	
	private long checkInterval;
	private String lsfCommand;
	
	//private LsfExitCodeMapper lsfProcessExitCodeMapper;
	private String command;
	private long timeout;

	private StepExecution execution;

	private SystemProcessExitCodeMapper systemProcessExitCodeMapper;
	

	/**
	 * @param command command to be executed in a separate system process
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @param envp environment parameter values, inherited from parent process
	 * when not set (or set to null).
	 */
	public void setEnvironmentParams(String[] envp) {
		this.environmentParams = envp;
	}

	/**
	 * @param dir working directory of the spawned process, inherited from
	 * parent process when not set (or set to null).
	 */
	public void setWorkingDirectory(String dir) {
		if (dir == null) {
			this.workingDirectory = null;
			return;
		}
		this.workingDirectory = new File(dir);
		Assert.isTrue(workingDirectory.exists(), "working directory must exist");
		Assert.isTrue(workingDirectory.isDirectory(), "working directory value must be a directory");

	}

	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(command, "'command' property value is required");
		Assert.notNull(systemProcessExitCodeMapper, "SystemProcessExitCodeMapper must be set");
		Assert.isTrue(timeout > 0, "timeout value must be greater than zero");
		Assert.notNull(taskExecutor, "taskExecutor is required");
	}

	/**
	 * @param systemProcessExitCodeMapper maps system process return value to
	 * <code>ExitStatus</code> returned by Tasklet.
	 * {@link SimpleSystemProcessExitCodeMapper} is used by default.
	 */
	public void setSystemProcessExitCodeMapper(SystemProcessExitCodeMapper systemProcessExitCodeMapper) {
		this.systemProcessExitCodeMapper = systemProcessExitCodeMapper;
	}

	/**
	 * @param timeout upper limit for how long the execution of the external
	 * program is allowed to last.
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * The time interval how often the tasklet will check for termination
	 * status.
	 * 
	 * @param checkInterval time interval in milliseconds (1 second by default).
	 */
	public void setTerminationCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	/**
	 * Get a reference to {@link StepExecution} for interrupt checks during
	 * system command execution.
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.execution = stepExecution;
	}

	/**
	 * Sets the task executor that will be used to execute the system command
	 * NB! Avoid using a synchronous task executor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * If <code>true</code> tasklet will attempt to interrupt the thread
	 * executing the system command if {@link #setTimeout(long)} has been
	 * exceeded or user interrupts the job. <code>false</code> by default
	 */
	public void setInterruptOnCancel(boolean interruptOnCancel) {
		this.interruptOnCancel = interruptOnCancel;
	}

}
