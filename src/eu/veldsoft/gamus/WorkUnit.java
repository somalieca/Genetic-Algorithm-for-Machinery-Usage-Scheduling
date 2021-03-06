package eu.veldsoft.gamus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Single work unit consists of jobs.
 * 
 * @author Todor Balabanov
 */
class WorkUnit {
	/**
	 * Pseudo random number generator.
	 */
	private static final Random PRNG = new Random();

	/**
	 * List of the available machines.
	 */
	private List<Machine> machines = new ArrayList<Machine>();

	/**
	 * List of operations taken for this job.
	 */
	private List<Job> jobs = new ArrayList<Job>();

	/**
	 * List of action references.
	 */
	private List<Action> actions = new ArrayList<Action>();

	/**
	 * Reference to data object for the problem to be solved.
	 */
	private Object data[][] = null;

	/**
	 * Constructor with all parameters.
	 * 
	 * @param data
	 *            Problem data object reference.
	 */
	public WorkUnit(Object[][] data) {
		this.data = data;
	}

	/**
	 * Data loading from an array.
	 */
	public void load() {
		/*
		 * Clear from previous use.
		 */
		machines.clear();
		jobs.clear();
		actions.clear();

		/*
		 * Load machines list.
		 */ {
			for (int j = 2; j < data[0].length; j++) {
				machines.add(new Machine(data[0][j].toString(), false, null));
			}
		}

		/*
		 * Load jobs list.
		 */ {
			for (int i = 1; i < data.length; i++) {
				if (data[i][0].equals("") == false) {
					jobs.add(new Job(data[i][0].toString()));
				}
			}
		}

		/*
		 * Load operations list.
		 */ {
			int i = 1;
			for (Job job : jobs) {
				while (i < data.length
						&& (job.getName().equals(data[i][0].toString()) || data[i][0].toString().equals(""))) {
					Operation previous = null;
					if (job.getOperations().size() > 0) {
						previous = job.getOperations().get(job.getOperations().size() - 1);
					}
					job.getOperations().add(new Operation(data[i][1].toString(), job, previous, null));
					i++;
				}
			}
		}

		/*
		 * Load actions list.
		 */ {
			int i = 1;
			for (Job job : jobs) {
				for (Operation operation : job.getOperations()) {
					for (int j = 2; j < data[i].length; j++) {
						Action previous = null;
						if (operation.getActions().size() > 0) {
							previous = operation.getActions().get(operation.getActions().size() - 1);
						}
						Action action = new Action(0, ((Integer) data[i][j]).intValue(), 0, false, machines.get(j - 2),
								operation);
						operation.getActions().add(action);
						actions.add(action);
					}

					i++;
					if (i >= data.length) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Select random start times of all actions.
	 * 
	 * @param min
	 *            Minimum random value to be used.
	 * @param max
	 *            Maximum random value to be used.
	 */
	public void adjustRandomTimes(int min, int max) {
		for (Job job : jobs) {
			for (Operation operation : job.getOperations()) {
				for (Action action : operation.getActions()) {
					action.setStart(min + PRNG.nextInt(max - min + 1));
					action.setEnd(action.getStart() + action.getDuration());
				}
			}
		}
	}

	/**
	 * Simulate work unit.
	 * 
	 * @param limit
	 *            Limit discrete time for the simulation.
	 * @return Counters with the problems found.
	 */
	public int[] simulate(int limit) {
		/*
		 * Count different problems found.
		 */
		int[] problems = new int[] { 0, 0, };

		for (int time = 0; time < limit; time++) {
			// System.out.print("=");

			// TODO Do the simulation.
			for (Action action : actions) {
				/*
				 * If any action in the operation list of actions is done we do
				 * not need to calculate current loop iteration.
				 */
				if (action.getOperation().isDone() == true) {
					continue;
				}

				/*
				 * If current operation has predecessor and the predecessor is
				 * not finished yet do not calculate the action.
				 */
				Operation previous = action.getOperation().getPrevious();
				if (previous != null && previous.isDone() == false) {
					continue;
				}

				/*
				 * It is time the action to be done.
				 */
				if (action.getStart() == time && action.getDuration() > 0 && action.isDone() == false) {
					if (action.getMachine().isOccupied() == false) {
						/*
						 * Do the action on the machine.
						 */
						action.getMachine().setOccupied(true);
						action.getMachine().setAction(action);
					} else {
						/*
						 * Keep track of machine occupied times.
						 */
						problems[0]++;
					}
				}

				/*
				 * It is time the action to be done.
				 */
				if (action.getEnd() == time) {
					if (action.getMachine().isOccupied() == true) {
						action.setDone(true);
						action.getMachine().setOccupied(false);
						action.getMachine().setAction(null);
					} else {
						/*
						 * Keep track of unused machine times.
						 */
						problems[1]++;
					}
				}
			}
		}
		// System.out.println();

		return problems;
	}

	/**
	 * Counting of unfinished operations.
	 * 
	 * @return Number of unfinished operation.
	 */
	public int numberOfUndoneOperations() {
		int counter = 0;

		for (Job job : jobs) {
			for (Operation operation : job.getOperations()) {
				if (operation.isDone() == false) {
					counter++;
				}
			}
		}

		return counter;
	}

	/**
	 * Report the result of the simulation.
	 * 
	 * @return Text with the report.
	 */
	public String report() {
		String result = "";

		for (Job job : jobs) {
			result += job;
			result += "\n";
			for (Operation operation : job.getOperations()) {
				result += "\t";
				result += operation;
				result += "\n";

				result += "\t";
				result += "\t";
				result += operation.getActiveAction();
				result += "\n";
			}
		}

		return result;
	}

	/**
	 * Total time used report.
	 * 
	 * @return Total time.
	 */
	public int totalTimeUsed() {
		int total = 0;

		for(Action action : actions) {
			if(action.isDone() == false) {
				continue;
			}
			
			if(action.getEnd() > total) {
				total = action.getEnd();
			}
		}
		
		return total;
	}
}
