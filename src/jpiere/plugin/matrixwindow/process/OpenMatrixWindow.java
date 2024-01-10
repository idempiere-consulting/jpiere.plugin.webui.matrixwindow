package jpiere.plugin.matrixwindow.process;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.session.SessionManager;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class OpenMatrixWindow extends SvrProcess {

	private int recordID = 0;
	
	@Override
	protected void prepare() {
		recordID = getRecord_ID();

	}

	@Override
	protected String doIt() throws Exception {
		
		if(recordID>0) {
			Env.setContext(getCtx(), "#RecordMatrix", recordID);
			
			AEnv.executeAsyncDesktopTask(new Runnable() {
				@Override
				public void run() {
					int formID = DB.getSQLValue(null, "SELECT AD_Form_ID FROM AD_Form WHERE AD_Form_UU='6f8155c2-8c44-4f62-a7f7-144620d8cd84'");//Attività Cantiere
					if (formID>0)
						SessionManager.getAppDesktop().openForm(formID);
					
				}
			});
			
		}
		
		return "";
	}

}
