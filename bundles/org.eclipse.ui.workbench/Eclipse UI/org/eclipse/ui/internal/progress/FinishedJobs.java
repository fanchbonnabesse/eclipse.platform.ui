package org.eclipse.ui.internal.progress;

import java.util.HashSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.ListenerList;


class FinishedJobs {
    
    static interface KeptJobsListener {
        void finished(JobInfo info);
        void removed(JobInfo info);
        void infoVisited();
    }

    private static FinishedJobs theInstance;
    private static ListenerList listeners= new ListenerList();
	private IJobProgressManagerListener listener;
	private HashSet keptjobinfos= new HashSet();
	private long timeStamp;
    
    
    static synchronized FinishedJobs getInstance() {
        if (theInstance == null)
            theInstance= new FinishedJobs();
        return theInstance;
    }
    
    private FinishedJobs() {
	    listener= new IJobProgressManagerListener() {
            public void addJob(JobInfo info) {
            }

            public void addGroup(GroupInfo info) {
            }

            public void refreshJobInfo(JobInfo info) {
            }

            public void refreshGroup(GroupInfo info) {
            }

            public void refreshAll() {
            }

            public void removeJob(JobInfo info) {
                Job job= info.getJob();
                if (job != null) {
                    Object prop= job.getProperty(NewProgressViewer.KEEP_PROPERTY);
                    if (prop instanceof Boolean) {
                        if (((Boolean)prop).booleanValue())
                            add(info);
                    }
                    IStatus status= job.getResult();
                    if (status != null && status.getSeverity() == IStatus.ERROR)
                    	add(info);
                }
            }

            public void removeGroup(GroupInfo group) {
            }

            public boolean showsDebug() {
                return false;
            }
	    };
	    ProgressManager.getInstance().addListener(listener);	
    }
       
    void addListener(KeptJobsListener l) {
        listeners.add(l);
    }

    void removeListener(KeptJobsListener l) {
        listeners.remove(l);
    }
    
    void add(JobInfo info) {
        if (!keptjobinfos.contains(info)) {
            keptjobinfos.add(info);
            timeStamp++;
            if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: added job");
            Object l[]= listeners.getListeners();
			for (int i= 0; i < l.length; i++) {
			    KeptJobsListener jv= (KeptJobsListener) l[i];
			    jv.finished(info);
			}
        }
    }

    void remove(KeptJobsListener sender, JobInfo info) {
        if (keptjobinfos.remove(info)) {
        	if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: sucessfully removed job");
            Object l[]= listeners.getListeners();
			for (int i= 0; i < l.length; i++) {
			    KeptJobsListener jv= (KeptJobsListener) l[i];
			    if (jv != sender)
			        jv.removed(info);
			}
        }
    }
    
    void refresh() {
    	if (NewProgressViewer.DEBUG) System.err.println("FinishedJobs: refresh");
        Object l[]= listeners.getListeners();
		for (int i= 0; i < l.length; i++) {
		    KeptJobsListener jv= (KeptJobsListener) l[i];
		    jv.infoVisited();
		}
    }

    JobInfo[] getJobInfos() {
        return (JobInfo[]) keptjobinfos.toArray(new JobInfo[keptjobinfos.size()]);
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
