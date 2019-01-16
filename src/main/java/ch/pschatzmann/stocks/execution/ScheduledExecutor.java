package ch.pschatzmann.stocks.execution;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.pschatzmann.dates.CalendarUtils;
import ch.pschatzmann.dates.DateRange;
import ch.pschatzmann.stocks.Context;
import ch.pschatzmann.stocks.accounting.IAccount;
import ch.pschatzmann.stocks.accounting.ManagedAccount;
import ch.pschatzmann.stocks.accounting.Transaction;

/**
 * 
 * Executes the trades based on the indicated cron schedule.
 * 
 * @author pschatzmann
 *
 */
public class ScheduledExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledExecutor.class);
	private StrategyExecutor executor;
	private Scheduler scheduler;
	private int count;

	public ScheduledExecutor() {
	}

	/**
	 * Default Constructor
	 * @param executor
	 */
	public ScheduledExecutor(StrategyExecutor executor) {
		this.executor = executor;
	}

	/**
	 * Schedules the trading job
	 * @param tradeSchedule
	 * @throws SchedulerException
	 */
	public void schedule(String tradeSchedule) throws SchedulerException {
		// stop the job if it has been started already
		stop();
		// setup and start
		setupTradeJob(tradeSchedule, getScheduler());
		System.out.println("The trading has been scheduled....");
	}

	/**
	 * Stops the trading job
	 * @throws SchedulerException
	 */
	public void stop() throws SchedulerException {
		getScheduler().deleteJob(new JobKey("trade-job", getGroupName()));
		System.out.println("The scheduled trading has been stopped");
	}

	protected Scheduler getScheduler() throws SchedulerException {
		if (scheduler == null) {
			SchedulerFactory sf = new StdSchedulerFactory();
			scheduler = sf.getScheduler();
		}
		return scheduler;
	}

	protected void setupTradeJob(String tradeSchedule, Scheduler scheduler) throws SchedulerException {
		JobDetail job = JobBuilder.newJob(TradingJob.class).withIdentity("trade-job", getGroupName()).build();
		job.getJobDataMap().put("ScheduledExecutor", this);
		;
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity("cronTrigger", getGroupName())
				.withSchedule(CronScheduleBuilder.cronSchedule(tradeSchedule)).build();
		scheduler.scheduleJob(job, trigger);
		scheduler.start();
	}

	/**
	 * Reset all strategies to trigger a recalculation
	 */
	public void reset() {
		Context.resetCache();
		// reset strategies to trigger a new evaluation at the next run			
		executor.getTradingStrategies().forEach(s -> s.resetHistory());			
	}
	
	/**
	 * Start the trading for today
	 * @return
	 */
	public List<Transaction> trade() {
		return trade(new Date());
	}


	/**
	 * Start the trading for the indicated date
	 * @param date
	 * @return
	 */
	public List<Transaction> trade(Date date) {
		LOG.info("trade "+date);
		// we might need to consider the prior day because the current day has not ended
		// yet and we have no end of day quotes
		DateRange dateRange = new DateRange(Context.date(CalendarUtils.priorWorkDay(new Date())), new Date());
		LOG.info("processing DateRange: " + dateRange);
		getExecutor().run(dateRange);
		return this.getAccount().getTransactions().stream().filter(t -> t.isActive() && dateRange.isValid(t.getDate()))
				.collect(Collectors.toList());
	}

	protected String getGroupName() {
		return this.getAccount().getId();
	}

	/**
	 * Returns the account (from the executor)
	 * @return
	 */
	public IAccount getAccount() {
		return this.getExecutor().getAccount();
	}

	/**
	 * Returns the StrategyExecutor
	 * @return
	 */
	public StrategyExecutor getExecutor() {
		return this.executor;
	}

	protected void setExecutor(StrategyExecutor ex) {
		this.executor = ex;
	}

	/**
	 * Determines how many times the job has been executed since the start
	 * @return
	 */
	public long getJobExecutionCount() {
		return count;
	}

	protected void incJobExecutionCount() {
		count++;
	}

	public static class TradingJob implements Job {
		private static final Logger LOG = LoggerFactory.getLogger(TradingJob.class);

		public TradingJob() {
		}

		@Override
		public synchronized void execute(JobExecutionContext context) throws JobExecutionException {
			LOG.info("---------------");
			ScheduledExecutor executor = (ScheduledExecutor) context.getMergedJobDataMap().get("ScheduledExecutor");
			executor.incJobExecutionCount();
			LOG.info("Executing Scheduled Trading Jobj");
			// reset cache so that we access new data with the next run
			executor.reset();
			Collection<Transaction> transactions = executor.trade();

			JobKey jobKey = context.getJobDetail().getKey();
			LOG.info("TradeJob " + jobKey + " executing at " + new Date());
			logTransactions(transactions);
			saveTransactions(executor, transactions);
			saveAccount(executor.getAccount());
			LOG.info("-----E N D-----");
			
		}

		private void logTransactions(Collection<Transaction> transactions) {
			if (transactions.isEmpty()) {
				LOG.info("-> No transactions for today!");
			} else {
				LOG.info("-> transactions");
				transactions.forEach(t -> LOG.info("---> "+t.toString()));	
			}
		}

		private void saveTransactions(ScheduledExecutor executor, Collection<Transaction> transactions)
				throws JobExecutionException {
			String path = Context.getProperty("tadingPath", "trades");
			String fileName = path + File.separator + executor.getAccount().getId()
					+ new SimpleDateFormat("yyyy-MM-dd-hh.mm").format(new Date()) + ".json";
			new File(path).mkdirs();
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			try {
				writer.writeValue(new File(fileName), transactions);
			} catch (Exception e) {
				throw new JobExecutionException(e);
			}
		}

		private void saveAccount(IAccount account) {
			if (account instanceof ManagedAccount) {
				try {
					((ManagedAccount) account).save();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

}
