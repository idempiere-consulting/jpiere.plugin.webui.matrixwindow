/******************************************************************************
 * Product: JPiere(ジェイピエール) - JPiere Plugins Matrix Window             *
 * Copyright (C) Hideaki Hagiwara All Rights Reserved.                        *
 * このプログラムはGNU Gneral Public Licens Version2のもと公開しています。    *
 * このプラグラムの著作権は萩原秀明(h.hagiwara@oss-erp.co.jp)が保持しており、 *
 * このプログラムを使用する場合には著作権の使用料をお支払頂く必要があります。 *
 * 著作権の使用料の支払い義務は、このプログラムから派生して作成された         *
 * プログラムにも発生します。 サポートサービスは                              *
 * 株式会社オープンソース・イーアールピー・ソリューションズで                 *
 * 提供しています。サポートをご希望の際には、                                 *
 * 株式会社オープンソース・イーアールピー・ソリューションズまでご連絡下さい。 *
 * http://www.oss-erp.co.jp/                                                  *
 *****************************************************************************/
package jpiere.plugin.matrixwindow.form;

import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;

import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.compiere.model.GridField;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModelMap;

/**
 * Transfer data from editor to GridTab
 * @author hengsin
 *
 */
public class JPMatrixDataBinder implements ValueChangeListener {

	private final static CLogger logger = CLogger.getCLogger(JPMatrixDataBinder.class);

	private ListModelMap<Object, Object>  viewModel;

	private ListModelMap<Object, Object>  convetionTable ;

	private HashMap<Integer,PO> 	tableModel;

	private HashMap<Integer,PO> 	dirtyModel;

	/**
	 *
	 * @param gridTab
	 */
	public JPMatrixDataBinder(ListModelMap<Object, Object> listModel
			,ListModelMap<Object, Object>  convetionTable ,HashMap<Integer,PO> tableModel,HashMap<Integer,PO> dirtyModel)
	{
		this.viewModel = listModel;
		this.convetionTable = convetionTable;
		this.tableModel = tableModel;
		this.dirtyModel = dirtyModel;
	}

	/**
	 * @param e
	 */
	public void valueChange(ValueChangeEvent e)//TODO:ViewModelの更新
    {

		Object newValue = e.getNewValue();

        Object source = e.getSource();
        if (source instanceof WEditor)
        {
        	// Elaine 2009/05/06
        	WEditor editor = (WEditor) source;
        	Component cmp = editor.getComponent();
        	String cmpId = cmp.getId();
        	String[] yx = cmpId.split("_");	//行と列の情報を取得
            	int y =Integer.valueOf(yx[0]);
            	int x =Integer.valueOf(yx[1]);
;
            //viewModelの更新	TODO:処理的に考えるとviewModelの更新は現段階では不要な気がする…。コンポ―ネントをSave()前に更新する場合には必要になると思う…。
        	ListModelMap.Entry<Object, Object>  viewModelRow = viewModel.getElementAt(y);
        	TreeMap<Integer,Object> viewModelRowData = (TreeMap<Integer,Object>)viewModelRow.getValue();
        	viewModelRowData.put(x, newValue);

        	//convetionTableからPOのIDを取得
        	ListModelMap.Entry<Object, Object>  conversionTableRow = convetionTable.getElementAt(y);
        	TreeMap<Integer,Object> conversionTableRowData = (TreeMap<Integer,Object>)conversionTableRow.getValue();
        	Object PO_ID = conversionTableRowData.get(x);

        	//Tableモデルの更新
        	PO po = tableModel.get(PO_ID);
        	po.set_ValueNoCheck(editor.getColumnName(), newValue);

        	//DirtyModelに追加
        	dirtyModel.put((Integer)PO_ID, po);


        	GridField gridField = editor.getGridField();

        	if(gridField != null)
        	{
        		if(!gridField.isEditable(true))
        		{
//            		if (logger.isLoggable(Level.CONFIG)) logger.config("(" + gridTab.toString() + ") " + e.getPropertyName());
        			return;
        		}
        	}
        	else if(!editor.isReadWrite())
        	{
//            	if (logger.isLoggable(Level.CONFIG)) logger.config("(" + gridTab.toString() + ") " + e.getPropertyName());
        		return;
        	}
        }
        else
        {
//          if (logger.isLoggable(Level.CONFIG)) logger.config("(" + gridTab.toString() + ") " + e.getPropertyName());
            return;
        }


    } // ValueChange

	/**************************************************************************
	 * Save Multiple records - Clone a record and assign new values to each
	 * clone for a specific column.
	 * @param ctx context
	 * @param tableName Table Name
	 * @param columnName Column for which value need to be changed
	 * @param recordId Record to clone
	 * @param values Values to be assigned to clones for the specified column
	 * @param trxName Transaction
	 * @throws Exception If error is occured when loading the PO or saving clones
	 *
	 * @author ashley
	 */
	protected void saveMultipleRecords(Properties ctx, String tableName,
			String columnName, int recordId, Integer[] values,
			String trxName) throws Exception
	{
//		if (values == null)
//		{
//			return ;
//		}
//
//		int oldRow = gridTab.getCurrentRow();
//		GridField lineField = gridTab.getField("Line");
//
//		for (int i = 0; i < values.length; i++)
//		{
//			if (!gridTab.dataNew(false))
//			{
//				throw new IllegalStateException("Could not create new row");
//			}
//
//			gridTab.setValue(columnName, values[i]);
//
//			if (lineField != null)
//			{
//				gridTab.setValue(lineField, 0);
//			}
//
//			if (!gridTab.dataSave(false))
//			{
//				throw new IllegalStateException("Could not update row");
//			}
//		}
//		gridTab.setCurrentRow(oldRow);
	}
}