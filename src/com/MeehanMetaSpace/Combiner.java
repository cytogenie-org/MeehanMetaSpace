
package com.MeehanMetaSpace;

import java.util.*;

/**
 * <p>Title: FacsXpert</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Herzenberg Lab, Stanford University</p>
 * @author Stephen Meehan
 * @version 1.0
 */

public abstract class Combiner {
    public int getNumberOfTrueCombinables(){
        return combinables.length;
    }
	public static int stopAt =1000000, warnAt=98000;
	public static final String MSG_NO_FEASIBLE_COMBINATION_FOUND=
		 "No <i>feasible</i> combination found.",
		 MSG_MAX_ITERATION="MAX iteration reached";

	protected boolean reachedMaxIteration(){
		return anomalies.contains(MSG_MAX_ITERATION);
	}

	protected void addIfNew(final String msg){
		if (!anomalies.contains(msg)){
			anomalies.add(msg);
		}
	}

	protected abstract boolean acceptFeasibleCombination();
	protected abstract boolean prepareForFirstCombination();
	protected abstract String explainMaxCombinableExceeded();

	public interface Combinable{
		boolean isBeforeFirstState();
		Combiner getNextCombiner();
		boolean isNextStateFeasible();
		void nextState();
		void rewindStates();
	}
	public static void resetHistory(final Combiner top){
        iterationHangingChecker=0;
		if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
			history=new StringBuilder(8000);
			history.append(top.summary(true));
		} else {
			history=new StringBuilder();
		}

	}

	public static String getHistory() {
		if (!COMBINATORIC_HISTORY_BROWSING || !combinatoricHistoryBrowsing){
			return "";
		}
		final String s = history.toString();
		history=new StringBuilder();
		return s;
	}

	protected final int maxCombinables;
	public boolean readyForFirstCombination=false;

	private Combinable []combinables=null;
   public Combiner(final int maxCombinables) {
		// must be one
		this.maxCombinables=maxCombinables > 1 ? maxCombinables : 1;
   }

	protected void _prepareForFirstCombination(){
		if (combinables != null ){
			for (int i=0; i<combinables.length; i++){
				combinables[i].rewindStates();
			}
		}
	}

	protected abstract Combinable[] getCombinableUnitsForSummary();

	protected void setCombinableUnits(final Combinable [] combinables){
		this.combinables=combinables;
	}

	protected final static boolean COMBINATORIC_HISTORY_BROWSING=false;
	protected static boolean combinatoricHistoryBrowsing=false;

	public static void setCombinatoricHistoryBrowsing(final boolean on){
		if (on && !COMBINATORIC_HISTORY_BROWSING){
			new Exception("Can not turn ON history browsing becase Combiner.java is compiled with COMBINATORIC_HISTORY_BROWSING=false.  This exception is merely a warning. It does not halt the exeuction path.").printStackTrace(System.err);
		}
		combinatoricHistoryBrowsing=on;
	}

	public boolean doFirstCombination() {
		userCancelled=false;
		if (anomalies.size()==0){
			if (prepareForFirstCombination()){
                readyForFirstCombination = false;
                if (combinables != null && combinables.length != 0) {
                    return nextCombination(0);
                }
                return true;
            }
		}
		return false;
	}

	public boolean doNextCombination(final boolean restarting) {
		if (anomalies.size() == 0) {
			if (!restarting) {
				//combinables[combinables.length - 1].nextState();
				//if (combinables[combinables.length - 1].isNextStateFeasible()) {
					return nextCombination(combinables.length - 1);
				//}
			} else {
				if (combinables.length>0){
					return nextCombination(combinables.length - 1);
				} else {
					System.out.println("Yeeouch");
				}
			}
		}
		return false;
	}

	public Boolean hasFeasibleCombination=null;
	protected final Collection<String> anomalies=new ArrayList<String>();
	public final Collection<String> getAnomalies(){
		return anomalies;
	}

	public static StringBuilder history = new StringBuilder();

	protected void clearAnomalyHistory(){
		anomalies.clear();
		hasFeasibleCombination=null;
	}


	private boolean isNextStateFeasible(final Combinable c){
		final Combiner combiner=c.getNextCombiner();
		if (!c.isBeforeFirstState()){
			if (combiner!=null){
				if (combiner.doNextCombination(false)){
					return true;
				}
			}
			c.nextState();
		}
		while (c.isNextStateFeasible()){
			if (combiner == null || combiner.doFirstCombination()){
				return true;
			}
			c.nextState();
		}
		return false;
	}

	public final String summary(boolean top){
		final StringBuilder sb;
		if (!top){
			sb=new StringBuilder(500);
			sb.append("<li>");
		} else {
			sb=new StringBuilder(2000);
			sb.append("<h2>Stain set contents and <a title='click to see history of combinatoric reasoning' href='#combinatoricHistory'>combinatoric rationale</a></h2>");
			sb.append("<h3>");
		}
		sb.append(toString());
		if (top){
			sb.append("</h3>");
		}
		final Combinable[]combinables=getCombinableUnitsForSummary();
		if (combinables!=null){
			sb.append(Basics.lineFeed);
			sb.append("<ol>");
			for (int i=0;i<combinables.length;i++){
				Combiner next=combinables[i].getNextCombiner();
				if (next != null){
					sb.append(next.summary(false));
				} else {
					sb.append("<li>");
					sb.append(combinables[i]);
				}
			}
			sb.append("</ol>");
			sb.append(Basics.lineFeed);
		}
		if (top){
	sb.append("<hr><h3><a name='combinatoricHistory'>Combinatoric reasoning history</a></h3>");
}

		return sb.toString();
	}

	private final boolean nextCombination(final int start){
		if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
			history.append(Basics.lineFeed);			
			history.append("<ol>");
		}
		hasFeasibleCombination=Boolean.TRUE;
		if (getNumberOfTrueCombinables()>maxCombinables){
			final String flaw=explainMaxCombinableExceeded();
			if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
				history.append(flaw);
			}
			anomalies.add(flaw);
			if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
				history.append("</ol>");
				history.append(Basics.lineFeed);
			}

			return false;
		}
		boolean ranOutOfOptions=false;
		for (int attempts = 0; attempts < 1 || (attempts == 1 && shouldAutomaticallyAcceptFeasibleCombinations()); attempts++) {
			ranOutOfOptions=false;
			int combinableIndex = start;
			if (attempts==1) {
				/*@todo
				 * in case this method (nextCombination()) was called by doNextCombination() and start>0 THEN
				 * it is EXTREMELY important  to restore the prior state of everything to what it was when attempts==0
				 * ... for now time is running out on deadline ... sigh 
				*/
				combinables[start].rewindStates();
			}
			while (combinableIndex < combinables.length && !userCancelled) {
				boolean isFeasible = isNextStateFeasible(combinables[combinableIndex]);
				iterationHangingChecker++;
				if (iterationHangingChecker > warnAt) { // are we hung?
					// System.out.println("om="+om+", combiner="+this);
					if (iterationHangingChecker > stopAt) {
						final String msg = "Maximum iteration of " + stopAt + " was reached ";
						addIfNew(msg);
						addIfNew(MSG_MAX_ITERATION);
						return false;
					}
				}
				if (!isFeasible) {
					if (combinableIndex == 0) {
						hasFeasibleCombination = Boolean.FALSE;
						ranOutOfOptions=true;
						break;
					}
					combinables[combinableIndex].rewindStates();
					combinableIndex--;
					continue;
				} else if (combinableIndex == combinables.length - 1) {
					if (attempts==0 && !acceptFeasibleCombination()) {
						do {
							isFeasible = isNextStateFeasible(combinables[combinableIndex]);		
						} while (isFeasible && !acceptFeasibleCombination());
						if (!isFeasible) {
						combinables[combinableIndex].rewindStates();
						if (combinableIndex == 0) {
							if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing) {
								history.append("</ol>");
								history.append(Basics.lineFeed);
							}
							ranOutOfOptions=true;
							break;
						}
						combinableIndex--;
						continue;
						}
					}
				}
				combinableIndex++;
			}
			if (!ranOutOfOptions) {
				break;
			}
		}
		if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
			history.append(Basics.lineFeed);
			history.append("</ol>");
			history.append(Basics.lineFeed);
		}
		if (userCancelled){
			anomalies.add("User cancelled operation.");
		}
		return !ranOutOfOptions || userCancelled;
	}
	
	protected boolean shouldAutomaticallyAcceptFeasibleCombinations() {
		return false;
	}

	/**
	 *
	 * @param noFeasibleMsg NULL to have the default message
	 * @return
	 */
	public String noteFlaw(final String noFeasibleMsg){
		final String actualMsgIamGoingToUse=noFeasibleMsg==null? MSG_NO_FEASIBLE_COMBINATION_FOUND:noFeasibleMsg;
		
		anomalies.add(actualMsgIamGoingToUse);
		browseCombinatoricsHistory(false);
		return actualMsgIamGoingToUse;
	}
	int shows=0;
	protected final void browseCombinatoricsHistory(final boolean success){
		if (COMBINATORIC_HISTORY_BROWSING && combinatoricHistoryBrowsing){
			final String answer;
			if (success){
				answer=Basics.toHtmlUncentered(
						"Combination succeeded", 
						"View combinatorics history in default browser?<br><small>(Note: internet explorer is unreliable for this, any other browser behaves professionally)</small>");
			}else {
				answer=Basics.toHtmlErrorUncentered(
						"Combination did NOT succeed", 
						"View combinatorics history in default browser?<br><small>(Note: internet explorer is unreliable for this, any other browser behaves professionally)</small>");

			}

			Basics.gui.showHtml(
						Basics.concatObjects("combiner_",shows++,"_"), 
				Basics.concat("<html><h3>Combinatorics dump</h3><hr>", getHistory(), "</html>"), 
				false);
			}

	}

	private static int iterationHangingChecker=0;

	protected boolean userCancelled=false;
	public void setUserCancelled(final boolean b){
		this.userCancelled=b;
	}
  public static int getIterations(){
	return iterationHangingChecker;
  }
}
