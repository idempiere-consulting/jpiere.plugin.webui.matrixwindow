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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import jpiere.plugin.matrixwindow.model.MMatrixField;
import jpiere.plugin.matrixwindow.model.MMatrixWindow;

import org.adempiere.base.IModelFactory;
import org.adempiere.base.Service;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.adwindow.GridTabRowRenderer;
import org.adempiere.webui.adwindow.GridView;
import org.adempiere.webui.adwindow.IADTabbox;
import org.adempiere.webui.adwindow.IADTabpanel;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.EditorBox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Reference;
import org.compiere.model.MColumn;
import org.compiere.model.MField;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MRefTable;
import org.compiere.model.MTab;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.SystemIDs;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.TrxRunnable;
import org.zkoss.zk.au.out.AuFocus;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Center;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.North;
import org.zkoss.zul.Space;

public class JPiereMatrixWindow extends AbstractMatrixWindowForm implements EventListener<Event>, ValueChangeListener,WTableModelListener{
	/**	Logger			*/
	public static CLogger log = CLogger.getCLogger(JPiereMatrixWindow.class);

	private CustomForm form = new CustomForm();

	/**********************************************************************
	 * 【Composer】
	 **********************************************************************/
	@Wire
	private WTableDirEditor AD_Org_Editor;
	@Wire
	private WSearchEditor Search_Field_Editor;
	@Wire
    private Button SearchButton;
	@Wire
    private Button SaveButton;


	/**********************************************************************
	 * 【UI Component】
	 **********************************************************************/
	//ラベル
	private Label AD_Org_Label = new Label();					//組織マスタラベル
	private Label Search_Field_Label =new Label();				//検索フィールドラベル

	//メインレイアウト
	private Borderlayout mainLayout = new Borderlayout();
	private Panel southPanel = new Panel();

	//パラメータパネル
	private Panel parameterPanel = new Panel();						//検索条件などを設定するパラメータパネル
	private Grid parameterLayout = GridFactory.newGridLayout();		//パラメータパネルのレイアウト

	//納品明細パネル
	private Panel deliveryLinePanel = new Panel();
	private Borderlayout deliveryLayout = new Borderlayout();

	//リストボックス
	Grid listbox  = new Grid();



	/**********************************************************************
	 * 【管理用変数】
	 * ここより下の変数は、マトリクスウィンドウの動作管理用変数です。
	 **********************************************************************/

	private boolean     m_calculating = false;

	PO[] m_POs;

	StringBuilder message = new StringBuilder();


	//View Model:画面表示用のデータモデルMAP<rowの識別子,<カラム番号,data>>
	TreeMap<Object,TreeMap<Integer,Object>> viewModel = new TreeMap<Object,TreeMap<Integer,Object>>() ;

	//Convetion Table:画面表示用データとテーブルのデータを結びつけるMAP<rowの識別子,<カラム番号,dataの識別子>>
	TreeMap<Object,TreeMap<Integer,Object>> conversionTable = new TreeMap<Object,TreeMap<Integer,Object>> ();

	//テーブルに対応するPOのインスタンスMAP<POのID,PO>
	HashMap<Integer,PO> 				tableModel = new HashMap<Integer,PO>();

	//保存しなければならないPOのインスタンスMAP<POのID,PO>
	HashMap<Integer,PO> 				dirtyModel  = new HashMap<Integer,PO>();

	//画面表示のためにキーカラム毎にモデルクラスのインスタンスを区分管理しているMAP
	//TreeMap<縦軸となるカラムの識別子,<横軸となるrowの識別子,PO>>
	TreeMap<Object,TreeMap<Object,PO>> keyColumnModel = new TreeMap<Object,TreeMap<Object,PO>>();


	/*【縦軸と横軸のキー情報】*/
	//リンクカラムと縦軸のキーと横軸のキーの３つでユニーク制約をつけておく前提です。
	//Columnキー(Columnの軸となるキー情報)のリスト
	ArrayList<Object> columnKeys = new ArrayList<Object>();
	 //Columnキーと名称のマップ
	HashMap<Object,String> columnKeyNameMap = new HashMap<Object,String>();

	//Rowキー(Rowの軸となるキー情報)のリスト
	ArrayList<Object> rowKeys = new ArrayList<Object>();


	//全部(固定カラム+変動カラム)のカラムの名称のマップ<カラム順番,カラム名称>
	HashMap<Integer,String> columnNameMap = new HashMap<Integer,String> ();
	//全部(固定カラム+変動カラム)のカラムのGridFieldのインスタンスのマップ<カラム順番,GridField>
	HashMap<Integer,GridField> columnGridFieldMap = new HashMap<Integer,GridField> ();

	//表領域に表示する固定項目のMAP<表示順番,項目(カラム)名> ※現在の仕様として、Rowの識別子となるカラムは1行に固定。
	TreeMap<Integer, String> fixItem = new TreeMap<Integer, String>();//TODO：SQLでパラメータ取得できるようにする。

	//固定項目(カラム)のAD_Field_IDのマップ<表示順番,AD_Field_ID>
	TreeMap<Integer,Integer> fixItemFieldIDMap = new TreeMap<Integer,Integer>();


	private String whereClause ;

	private JPMatrixGridRowRenderer renderer;

	private String columnOnClick;


	/**********************************************************************
	 * 【パラメータ設定項目】
	 * ここより下のプロパティ変数を変更すれば、汎用的に活用できます。
	 **********************************************************************/

	MMatrixWindow 	m_matrixWindow;
	MTab			m_Tab;
	MMatrixField[]  m_matrixFields ;
	MField[]		m_contentFields;
	MColumn[]		m_contentColumns;

	/*【パラメータ設定用定数】*/
	final static String CLASS_INTTEGER = "Integer";
	final static String CLASS_STRING = "String";
	final static String CLASS_TIMESTAMP = "Timestamp";


	//AD_Window_IDを設定する
	private int AD_WINDOW_ID = 0;

	//テーブル名を設定する
	private String TABLE_NAME ;

	//親テーブルとのリンクカラムを設定
	private String LINK_COLUMN ;


	/*縦軸と横軸のパターンデータ1*/

	//Columnキーの型
	private String COLUMN_KEY_CLASS ; //TODO
	//縦軸となるカラムを設定する
	private String COLUMN_KEY_NAME;
	//縦軸となるカラムがテーブル名_IDの場合、そのテーブル名を設定する。COLUMN_KEY_NAMEがテーブル名_IDの場合は設定必須。
	private String COLUMN_KEY_TABLE;

	//Rowキーの型
	private String ROW_KEY_CLASS  ;	//TODO
	//横軸となるカラムを設定する
	private String ROW_KEY_NAME;
	//横軸となるカラムがテーブル名_IDの場合、そのテーブル名を設定する
	private String ROW_KEY_TABLE = "";

	private int FIX_ITEM_FIELD_ID = 0;


	/****************************************************
	 * Window情報
	 ****************************************************/

	private CustomForm window = new CustomForm();
	private ADWindow adWindow;
	private IADTabpanel adTabpanel;
	private GridTab gridTab ;
	private GridField[] gridFields ;

    public JPiereMatrixWindow() throws IOException
    {
    	;
    }

	public ADForm getForm()
	{
		return form;
	}

	public void createMatrixWindow(String Value)
	{
    	try
		{
    		prepare(Value);
			dynInit();
			zkInit();
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}

	}

	public void prepare(String Value) throws Exception
	{
		m_matrixWindow = MMatrixWindow.get(Env.getCtx(), Value);
		if(m_matrixWindow == null)
		{
			;//TODO エラー処理
		}

		m_matrixFields = m_matrixWindow.getMatrixFields();
		m_contentFields = m_matrixWindow.getContentFields();
		m_contentColumns = new MColumn[m_contentFields.length];
		for(int i = 0; i < m_contentFields.length; i++)
		{
			m_contentColumns[i]=new MColumn(Env.getCtx(),m_contentFields[i].getAD_Column_ID(),null);
		}


		AD_WINDOW_ID = m_matrixWindow.getAD_Window_ID();
		m_Tab = new MTab(Env.getCtx(), m_matrixWindow.getAD_Tab_ID(), null);
		TABLE_NAME = MTable.get(Env.getCtx(), m_Tab.getAD_Table_ID()).getTableName();
		LINK_COLUMN = MColumn.get(Env.getCtx(), m_Tab.getAD_Column_ID()).getColumnName();
		if(LINK_COLUMN==null)
		{
			;//TODO エラー処理
		}

		I_AD_Column keyColumn = m_matrixWindow.getJP_MatrixColumnKey().getAD_Column();

		COLUMN_KEY_NAME = keyColumn.getColumnName();

		if(keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TABLEDIR)
		{
			COLUMN_KEY_TABLE = COLUMN_KEY_NAME.substring(0, COLUMN_KEY_NAME.length() - "_ID".length());
			COLUMN_KEY_CLASS = CLASS_INTTEGER;

		}else if( keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TABLE
				|| keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_SEARCH )
		{
			if(keyColumn.getAD_Reference_Value_ID()==0)
			{
				COLUMN_KEY_TABLE = COLUMN_KEY_NAME.substring(0, COLUMN_KEY_NAME.length() - "_ID".length());
			}else{

				I_AD_Reference ref = keyColumn.getAD_Reference_Value();
				MRefTable refTable =new MRefTable(Env.getCtx(), ref.getAD_Reference_ID(), null);
				COLUMN_KEY_TABLE = refTable.getAD_Table().getTableName();

			}
			COLUMN_KEY_CLASS = CLASS_INTTEGER;
		}else if( keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_DATE
				|| keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_DATETIME
				|| keyColumn.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TIME ){
			COLUMN_KEY_CLASS = CLASS_TIMESTAMP;
		}else{
			COLUMN_KEY_CLASS = CLASS_STRING;//TODO:まとめ過ぎ…
		}

		I_AD_Column keyRow = m_matrixWindow.getJP_MatrixRowKey().getAD_Column();

		ROW_KEY_NAME = keyRow.getColumnName();

		if(keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TABLEDIR )
		{
			ROW_KEY_TABLE = ROW_KEY_NAME.substring(0, COLUMN_KEY_NAME.length() - "_ID".length());
			ROW_KEY_CLASS = CLASS_INTTEGER;

		}else if( keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TABLE
				|| keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_SEARCH )
		{
			if(keyRow.getAD_Reference_Value_ID()==0)
			{
				ROW_KEY_TABLE = ROW_KEY_NAME.substring(0, COLUMN_KEY_NAME.length() - "_ID".length());
			}else{
				I_AD_Reference ref = keyRow.getAD_Reference_Value();
				MRefTable refTable =new MRefTable(Env.getCtx(), ref.getAD_Reference_ID(), null);
				ROW_KEY_TABLE = refTable.getAD_Table().getTableName();
			}

			ROW_KEY_CLASS = CLASS_INTTEGER;
		}else if( keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_DATE
				|| keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_DATETIME
				|| keyRow.getAD_Reference_ID()==SystemIDs.REFERENCE_DATATYPE_TIME ){
			ROW_KEY_CLASS = CLASS_TIMESTAMP;
		}else{
			ROW_KEY_CLASS = CLASS_STRING;//TODO:まとめ過ぎ…
		}

		FIX_ITEM_FIELD_ID = m_matrixWindow.getJP_MatrixRowKey().getAD_Field_ID();


	}

	public void dynInit() throws Exception
	{
		int AD_Column_ID = MColumn.getColumn_ID("C_BPartner", "AD_Org_ID");
		MLookup lookupOrg = MLookupFactory.get(Env.getCtx(), form.getWindowNo(), 0, AD_Column_ID, DisplayType.TableDir);
		AD_Org_Editor = new WTableDirEditor("AD_Org_Editor", true, false, true, lookupOrg);
		AD_Org_Editor.setValue(Env.getAD_Org_ID(Env.getCtx()));

		AD_Column_ID = MColumn.getColumn_ID(TABLE_NAME, LINK_COLUMN);
		MLookup lookup = MLookupFactory.get(Env.getCtx(), form.getWindowNo(), 0, AD_Column_ID, DisplayType.Search);
		Search_Field_Editor = new WSearchEditor("Search_Field_Editor", true, false, true, lookup);
//		Search_Field_Editor.setValue(1000000);

		SearchButton = new Button("検索");
		SearchButton.setId("SearchButton");
		SearchButton.addActionListener(this);

		SaveButton = new Button("保存");
		SaveButton.setId("SavetButton");
		SaveButton.addActionListener(this);

		/*表領域に表示する項目<表示順番,項目(カラム)名>を取得する*/
		//fixItemはRowの識別子となるカラムの1行で固定
		fixItem.put(0, ROW_KEY_NAME);
		fixItemFieldIDMap.put(0,FIX_ITEM_FIELD_ID);

	}


	private void zkInit() throws Exception
	{
		form.appendChild(mainLayout);

		/*【メインレイアウト(Borderlayout)】*/
		mainLayout.setWidth("99%");
		mainLayout.setHeight("100%");

		//【メインレイアウト(Borderlayout)-北】
		North north = new North();
		mainLayout.appendChild(north);



		//パラメータパネル
		north.appendChild(parameterPanel);
		north.setStyle("border: none");
		parameterPanel.appendChild(parameterLayout); 		//parameterLayout = Grid
		parameterLayout.setWidth("90%");
		Rows parameterLayoutRows = parameterLayout.newRows();
		Row row = parameterLayoutRows.newRow();
			Groupbox searchGB = new Groupbox();
			row.appendCellChild(searchGB,8);
			searchGB.appendChild(new Caption("検索条件"));
			Grid searchGrid  = new Grid();
			searchGrid.setStyle("background-color: #E9F0FF");
			searchGrid.setStyle("border: none");
			searchGB.appendChild(searchGrid);
			Rows rows = searchGrid.newRows();

			//パラメータパネル-1段目(検索条件パネル)
			row = rows.newRow();
				AD_Org_Label.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));	//組織ラベル
				row.appendCellChild(AD_Org_Label.rightAlign());
				row.appendCellChild(AD_Org_Editor.getComponent(),1);
				Search_Field_Label.setText(Msg.translate(Env.getCtx(), LINK_COLUMN));	//検索フィールドラベル
				row.appendCellChild(Search_Field_Label.rightAlign());
				row.appendCellChild(Search_Field_Editor.getComponent(),1);

		row = parameterLayoutRows.newRow();
				row.appendCellChild(SearchButton);
				row.appendCellChild(SaveButton);

		row = parameterLayoutRows.newRow();
				row.appendCellChild(new Space(),1);//ボタンの下に空白行を入れているだけ。

		Center center = new Center();
		mainLayout.appendChild(center);
		center.appendChild(deliveryLinePanel);
		deliveryLinePanel.appendChild(deliveryLayout);//Borderlayout
		deliveryLinePanel.setWidth("100%");
		deliveryLinePanel.setHeight("100%");
		deliveryLinePanel.setHflex("1");
		deliveryLinePanel.setVflex("1");
		deliveryLayout.setWidth("100%");
		deliveryLayout.setHeight("100%");
		deliveryLayout.setStyle("border: none");

		//情報パネル-北：請求書パネル-中央
				center = new Center();
				deliveryLayout.appendChild(center);
				center.appendChild(listbox);
				listbox.setWidth("99%");
				listbox.setHeight("99%");
				center.setStyle("border: none");

				listbox.setWidth("100%");
				listbox.setHeight("100%");
				listbox.setSizedByContent(false);
				listbox.setVflex(true);

	}


	@Override
	public void tableChanged(WTableModelEvent e) {
		int row = e.getFirstRow();
		int col = e.getColumn();
		ListModel<?> listModel = e.getModel();

		updateModel(listModel,row, col);

		ListModel listModelMapTable = listbox.getModel();
//		listModelMapTable.updateComponent(row);


		m_calculating = false;

	}

	private void updateModel(ListModel<?> listmodel ,int row, int col) {

		if (m_calculating)  //  Avoid recursive calls
			return ;

		if(col < fixItemFieldIDMap.size())
		{
			m_calculating = false;
			return;

		}else{

			Map.Entry<?,?> mapEntry = (Map.Entry<?,?>)listmodel.getElementAt(row);
			Object mapKey = mapEntry.getKey();
			TreeMap<?,?> mapValue = (TreeMap<?,?>)mapEntry.getValue();
			Object value = mapValue.get(col);
			Object poID = conversionTable.get(mapKey).get(col);
			PO po = tableModel.get(poID);
//			String columnName = columnNameMap.get(col);
			String columnName = columnGridFieldMap.get(col).getColumnName();
			po.set_ValueNoCheck(columnName, value);
			dirtyModel.put(po.get_ID(), po);


			m_calculating = true;
			return;
		}
	}

	@Override
	public void valueChange(ValueChangeEvent evt) {
		;
	}

	@Override
	public void onEvent(Event e) throws Exception {

		message = new StringBuilder();

		if (e == null)
		{
			return;
		}
		else if (e.getTarget() == listbox && Events.ON_CLICK.equals(e.getName()))
		{
			Object data = e.getData();
			org.zkoss.zul.Row row = null;
			String columnName = null;
			if (data != null && data instanceof Component)
			{
				if (data instanceof org.zkoss.zul.Row)
					row = (org.zkoss.zul.Row) data;
				else
				{
					AbstractComponent cmp = (AbstractComponent) data;
					if (cmp.getParent() instanceof org.zkoss.zul.Row)
					{
						row = (Row) cmp.getParent();
						columnName = (String) cmp.getAttribute("columnName");
					}
				}
			}
			if (row != null)
			{
				//click on selected row to enter edit mode
				if (row == renderer.getCurrentRow())
				{
					if (!renderer.isEditing())
					{
						renderer.editCurrentRow();
						if (columnName != null && columnName.trim().length() > 0)
							setFocusToField(columnName);
						else
							renderer.focusToFirstEditor();
					}
				}
				else
				{
					int index = listbox.getRows().getChildren().indexOf(row);
					if (index >= 0 ) {
						columnOnClick = columnName;
						onSelectedRowChange(index);
					}
				}
			}
			e.stopPropagation();
        }
//		else if (e.getTarget() == paging)
//		{
//			int pgNo = paging.getActivePage();
//			if (pgNo != listModel.getPage())
//			{
//				listModel.setPage(pgNo);
//				onSelectedRowChange(0);
//				gridTab.clearSelection();
//				Clients.resize(listbox);
//			}
//		}
//		else if (e.getTarget() == selectAll)
//		{
//			toggleSelectionForAll(selectAll.isChecked());
//		}
		else if (e.getName().equals("onSelectRow"))
		{
			Checkbox checkbox = (Checkbox) e.getData();
			int rowIndex = (Integer) checkbox.getAttribute(GridTabRowRenderer.GRID_ROW_INDEX_ATTR);
			if (checkbox.isChecked())
			{
//				gridTab.addToSelection(rowIndex);
//				if (!selectAll.isChecked() && isAllSelected())
//				{
//					selectAll.setChecked(true);
//				}
			}
			else
			{
//				gridTab.removeFromSelection(rowIndex);
//				if (selectAll.isChecked())
//					selectAll.setChecked(false);
			}

		}else if (e.getTarget().equals(SearchButton))
		{
			if(!createView ())
			{
				throw new Exception(message.toString());
			}

		}else if(e.getTarget().equals(SaveButton)){

			boolean isOK = saveData();

			if(isOK)
			{
				dirtyModel.clear();
				if(!createView ())
				{
					throw new Exception(message.toString());
				}
			}else{
				;//saveData()メソッド内でエラー処理しているのでここでのエラー処理は不要
			}

		}

	}


	private boolean createView () throws ClassNotFoundException {

		//Windowの情報を使うので、画面上には表示させませんが、ウィンドウを作成します。
		if(adWindow==null)
		{
			adWindow = new ADWindow(Env.getCtx(),AD_WINDOW_ID, null);
			adWindow.createPart(window);
		}
		ADWindowContent adWindowContent = adWindow.getADWindowContent();
		IADTabbox adTabbox = adWindowContent.getADTab();
		adTabpanel = adTabbox.getADTabpanel(1);
		gridTab = adTabpanel.getGridTab();
		GridView gridView = adTabpanel.getGridView();
		gridFields = gridTab.getFields();


		//Where句の作成(数か所で使用するので1か所で宣言しておく）
		whereClause = createWhere();
		if(message.length() > 0)
			return false;

		//columnKeysの作成
		columnKeys = createColumnKeys(whereClause);
		if(columnKeys.size()==0)
		{
			message.append("データがありません");//要多言語化
			return false;
		}

		//rowKeysの作成
		rowKeys = createRowKeys(whereClause);
		if(rowKeys.size()==0)
		{
			message.append("データがありません");//要多言語化
			return false;
		}

		//getLiesでTable Modelのハッシュマップを作成
		m_POs = getPOs(whereClause,true);
		if(m_POs.length==0)
		{
			message.append("データがありません");//要多言語化
			return false;
		}

		tableModel = createTableModel(m_POs);
		keyColumnModel = createKeyColumnModel(m_POs);

		//rowKeysだけを設定した空のハッシュマップのView Modelの作成
		viewModel = createViewModelConvetionTable();
		conversionTable = createViewModelConvetionTable();

		//rowKeysが設定されているView Modelに、Table Modelから値を設定し、Convetion Tableを作成する。
		setViewModelConvetionTable();


		//JPWListBoxの初期化
//		listbox.clear();
//		listbox.getModel().removeTableModelListener(this);

		JPListModelMapTable vmListModelMap = new JPListModelMapTable(viewModel);
		JPListModelMapTable ctListModelMap = new JPListModelMapTable(conversionTable);

		vmListModelMap.addTableModelListener(this);		//JPListModelMapTable#setDataAt()処理から、this#tableChanged()メソッドが呼び出される。
		listbox.setModel(vmListModelMap);

		org.zkoss.zul.Columns columns = listbox.getColumns();
		if(columns==null)
		{
			Auxhead auxhead = createAuxhead();
			listbox.appendChild(auxhead);

			createtColumnMap();
			Columns clms = createColumns();
			listbox.appendChild(clms);

			Frozen frozen = new Frozen();
			frozen.setColumns(fixItemFieldIDMap.size());
			listbox.appendChild(frozen);
		}

		renderer = new JPMatrixGridRowRenderer(vmListModelMap,ctListModelMap,tableModel,dirtyModel, form.getWindowNo());
		renderer.setcColumnsSize(columnNameMap.size());					//TODO:コンストラクタの引数とする
		renderer.gridView = gridView;									//TODO:コンストラクタの引数とする
		renderer.gridTab = gridTab;										//TODO:コンストラクタの引数とする
		renderer.columnGridFieldMap = columnGridFieldMap;				//TODO:コンストラクタの引数とする
		listbox.setRowRenderer(renderer);

		return true;

	}


	private String createWhere(){
		Object oo = AD_Org_Editor.getValue();
		Object dd = Search_Field_Editor.getValue();

		if(AD_Org_Editor.getValue() == null)
		{
			message.append("組織は必須入力です。");//要多言語化
		}

		if(Search_Field_Editor.getValue() == null || (Integer)Search_Field_Editor.getValue() == 0)
		{
			message.append(LINK_COLUMN + "は必須入力です。");//要多言語化
		}

		String whereClause = " WHERE AD_Org_ID = " + AD_Org_Editor.getValue() +" AND "
							+ LINK_COLUMN  + " = " + Search_Field_Editor.getValue() ;

		return whereClause;
	}


	private ArrayList<Object> createColumnKeys(String whereClause)
	{
		ArrayList<Object> list = new ArrayList<Object>();
		final String sql = "SELECT DISTINCT " + COLUMN_KEY_NAME +" FROM " + TABLE_NAME + whereClause
							+ " ORDER BY " + COLUMN_KEY_NAME;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				if(COLUMN_KEY_CLASS.equals(CLASS_INTTEGER))
				{
					list.add(rs.getInt(1));
					if(COLUMN_KEY_NAME.endsWith("_ID"))
					{
						columnKeyNameMap.put(rs.getInt(1), getNameFromTable(COLUMN_KEY_TABLE, rs.getInt(1)));

					}
				}else if(COLUMN_KEY_CLASS.equals(CLASS_STRING)){
					list.add(rs.getString(1));
				}else if(COLUMN_KEY_CLASS.equals(CLASS_TIMESTAMP)){
					list.add(rs.getTimestamp(1));
				}else{
					list.add(rs.getObject(1));
				}
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}

	private ArrayList<Object> createRowKeys(String whereClause)
	{
		ArrayList<Object> list = new ArrayList<Object>();
		final String sql = "SELECT DISTINCT " + ROW_KEY_NAME +" FROM " + TABLE_NAME + whereClause
							+ " ORDER BY " + ROW_KEY_NAME;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				if(ROW_KEY_CLASS.equals(CLASS_INTTEGER)){
					list.add(rs.getInt(1));
				}else if(ROW_KEY_CLASS.equals(CLASS_STRING)){
					list.add(rs.getString(1));
				}else if(ROW_KEY_CLASS.equals(CLASS_TIMESTAMP)){
					list.add(rs.getTimestamp(1));
				}else{
					list.add(rs.getObject(1));
				}
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		return list;
	}

	public PO[] getPOs (String whereClause,boolean reload)
	{
		if (reload || m_POs == null || m_POs.length == 0)
			;
		else
			return m_POs;
		//
		ArrayList<PO> list = new ArrayList<PO>();

		final String sql = "SELECT *  FROM " + TABLE_NAME + whereClause
				+ " ORDER BY " + COLUMN_KEY_NAME + "," + ROW_KEY_NAME;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			rs = pstmt.executeQuery();

			List<IModelFactory> factoryList = Service.locator().list(IModelFactory.class).getServices();
			if (factoryList == null)
			{
				;//エラー処理書いた方が良いのだろうけど、factoryListはあるのが前提という事で…。
			}
			PO po = null;
			while (rs.next())
			{
				for(IModelFactory factory : factoryList) {
					po = factory.getPO(TABLE_NAME, rs, null);//trxNameは保存時に取得されるので、ここでは不要と判断。
					if (po != null)
					{
						list.add(po);
						break;
					}
				}//for
			}//while

		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}

		m_POs = new PO[list.size()];
		list.toArray(m_POs);
		return m_POs;
	}	//	getLocations


	/*
	 * POのIDをキーとしたPOインスタンスのマップ
	 *
	 *
	 */
	private HashMap<Integer, PO> createTableModel(PO[] POs)
	{

		for(int i = 0; i < POs.length; i++)
		{
			tableModel.put(POs[i].get_ID(), POs[i]);
		}

		return tableModel;
	}

	/*
	 * 縦軸となるKey Column毎のPOのMAPを作成しいます。
	 * 作成もとの情報となるPOの配列は、縦軸となるKey Columnと
	 * 横軸となるKey Rowで、ソートされているのが前提の処理です。
	 *
	 * @return TreeMap<Key Column,TreeMap<Key Row,PO>>
	 */
	private TreeMap<Object,TreeMap<Object, PO>> createKeyColumnModel(PO[] POs)
	{
		Object columnKey = "";
		TreeMap<Object, PO> obj = null;	//<Rowの識別子,PO>

		for(int i = 0; i < POs.length; i++)
		{
			if(columnKey.equals(POs[i].get_Value(COLUMN_KEY_NAME)))
			{
				obj.put(POs[i].get_Value(ROW_KEY_NAME), POs[i]);
			}else{
				obj = new TreeMap<Object, PO> ();
				obj.put(POs[i].get_Value(ROW_KEY_NAME), POs[i]);
				columnKey = POs[i].get_Value(COLUMN_KEY_NAME);
				keyColumnModel.put(columnKey, obj);
			}
		}

		return keyColumnModel;
	}

	/*
	 * View ModelとConvetion Tableを作成するメソッドです。
	 * View ModelとConvetion Tableは同じTreeMap構造になります。
	 * 横軸のキー情報が設定され、必要となる縦軸のカラム数が確保されます。
	 *
	 * @return TreeMap<y,TreeMap<x,object>>
	 */
	private TreeMap<Object,TreeMap<Integer,Object>> createViewModelConvetionTable()
	{

		TreeMap<Object,TreeMap<Integer,Object>> y = new TreeMap<Object,TreeMap<Integer,Object>>();

		for(int i = 0; i < rowKeys.size(); i++)
		{
			TreeMap<Integer,Object> x = new TreeMap<Integer,Object>();
			Object obj = rowKeys.get(i);
			y.put(obj,x);
			x.put(0, obj);

			int columnNum = columnKeys.size() * m_contentFields.length;
			for(int j = 1; j < columnNum+1; j++)
			{
				x.put(j, null);
			}//for j

		}//for i

		return y;

	}

	private void setViewModelConvetionTable()
	{
		//画面表示のためにキーカラム毎にモデルクラスのインスタンスを区分管理しているMAP
		//TreeMap<縦軸となるカラムの識別子,<横軸となるrowの識別子,PO>>
		Set<Object> keyColumnModelKeySet = keyColumnModel.keySet();
		int i = 0;
		for(Object keyColumn :keyColumnModelKeySet)
		{
			//TreeMap<rowの識別子,PO>
			TreeMap<Object,PO> POs = keyColumnModel.get(keyColumn);
			Set<Object>  rowKeys = POs.keySet();
			for(Object rowKey : rowKeys)
			{
				//TreeMap<カラム番号,Data>
				TreeMap<Integer,Object> vmRow = viewModel.get(POs.get(rowKey).get_Value(ROW_KEY_NAME));
				TreeMap<Integer,Object> ctRow = conversionTable.get(POs.get(rowKey).get_Value(ROW_KEY_NAME));


				/*固定カラムの処理*/
				//現在の仕様では固定カラムは１カラムに限定されており、その情報はすでにputされているので、コメントアウトしておく。
//				if(i==0)
//				{
//					for(int j = 0; j < fixItem.size(); j++)
//					{
//							vmRow.put(j, POs.get(rowKey).get_Value(fixItem.get(j)));
//							ctRow.put(j, POs.get(rowKey).get_Value(fixItem.get(j)));
//					}
//				}

				/*変動カラムの処理*/
				for(int k = 0; k < m_contentFields.length; k++)
				{
					vmRow.put(fixItemFieldIDMap.size()+i*m_contentFields.length+k, POs.get(rowKey).get_Value(m_contentColumns[k].getColumnName()));
					ctRow.put(fixItemFieldIDMap.size()+i*m_contentFields.length+k, POs.get(rowKey).get_ID());
				}

			}//for(Object rowKey : rowKeys)

			i++;//縦軸となるキーカラムの処理カウンター

		}//for(Object keyColumn :keyColumnModelKeySet)

		return ;
	}

	private Auxhead createAuxhead() throws ClassNotFoundException{//TODO

		Auxhead auxhead = new Auxhead();

		//固定カラムの部分は仕様上1行とし、空白にする。
		Auxheader fix = new Auxheader("");
		auxhead.appendChild(fix);
		fix.setColspan(fixItemFieldIDMap.size());

		if(COLUMN_KEY_CLASS.equals(CLASS_INTTEGER) && COLUMN_KEY_NAME.endsWith("_ID"))
		{
			for(int i = 0 ; i < columnKeys.size(); i++)
			{
				Auxheader auxheader = new Auxheader(columnKeyNameMap.get(columnKeys.get(i)));
				auxhead.appendChild(auxheader);
				auxheader.setColspan(m_contentFields.length);
				auxheader.setAlign("center");
			}
		}
		else if(COLUMN_KEY_CLASS.equals(CLASS_STRING) )
		{
			for(int i = 0 ; i < columnKeys.size(); i++)
			{
				Auxheader auxheader = new Auxheader((String)columnKeys.get(i));
				auxhead.appendChild(auxheader);
				auxheader.setColspan(m_contentFields.length);
				auxheader.setAlign("center");
			}
		}
		else if(COLUMN_KEY_CLASS.equals(CLASS_TIMESTAMP) )
		{
			for(int i = 0 ; i < columnKeys.size(); i++)
			{
				Auxheader auxheader = new Auxheader(columnKeys.get(i).toString());
				auxhead.appendChild(auxheader);
				auxheader.setColspan(m_contentFields.length);
				auxheader.setAlign("center");
			}
		}else{
			;//auxheadなし
		}


		return auxhead;
	}


	private Columns createColumns()
	{

		Columns clms = new Columns();

		for(int i = 0; i < columnNameMap.size(); i++)
		{
			Column col = new Column(columnNameMap.get(i));
			col.setWidth("100px");//TODO:表示するカラムの幅を変数で指定できるようにする。
			col.setDraggable("true");
			clms.appendChild(col);
		}

		return clms;
	}


	private String getNameFromTable(String tableName, int Record_ID){

		List<IModelFactory> factoryList = Service.locator().list(IModelFactory.class).getServices();
		if (factoryList == null)
		{
			;//エラー処理書いた方が良いのだろうけど、factoryListはあるのが前提という事で…。
		}

		PO po = null;
		for(IModelFactory factory : factoryList) {
			po = factory.getPO(tableName, Record_ID, null);//trxNameは保存時に取得されるので、ここでは不要と判断。
			if (po != null)
				break;
		}//for

		StringBuilder name= new StringBuilder();
		int valueIndex = po.get_ColumnIndex("Value");
		if(valueIndex != -1)
		{
			name.append(po.get_Value(valueIndex));
		}

		int nameIndex = po.get_ColumnIndex("Name");
		if(nameIndex != -1)
		{
			if(!name.toString().isEmpty() && name.toString().length() > 0)
				name.append("_");

			name.append(po.get_Value(nameIndex));
		}

		return name.toString();
	}

	private void createtColumnMap()
	{
		int c = 0;	//	カラムマップ作成用カウンター

		//固定カラムの処理
		for(int i = 0; i < fixItemFieldIDMap.size(); i++)
		{
			columnNameMap.put(c, Msg.getElement(Env.getCtx(), fixItem.get(i)));
			for(int j = 0; j < gridFields.length; j++)
			{
				if(fixItemFieldIDMap.get(i).intValue() == gridFields[j].getAD_Field_ID())
					columnGridFieldMap.put(c, gridFields[j]);
			}
			c++;
		}

		//変動カラムの処理
		for(int i = 0; i < columnKeys.size(); i++)
		{
			for(int j = 0; j < m_contentFields.length; j++)
			{
				columnNameMap.put(c, Msg.getElement(Env.getCtx(), m_contentColumns[j].getColumnName()));
				for(int k = 0; k < gridFields.length; k++)
				{
					if(m_contentFields[j].getAD_Field_ID()==gridFields[k].getAD_Field_ID())
						columnGridFieldMap.put(c, gridFields[k]);
				}//k
				c++;
			}//j
		}//i

		return;
	}

	/**
	 * @param columnName
	 */
	public void setFocusToField(String columnName) {
		for (WEditor editor : renderer.getEditors()) {
			if (columnName.equals(editor.getColumnName())) {
				Component c = editor.getComponent();
				if (c instanceof EditorBox) {
					c = ((EditorBox)c).getTextbox();
				} else if (c instanceof NumberBox) {
					c = ((NumberBox)c).getDecimalbox();
				}
				Clients.response(new AuFocus(c));
				break;
			}
		}
	}

	private void onSelectedRowChange(int index) {
		if (updateModelIndex(index)) {
			updateListIndex();
		}
	}

	private boolean updateModelIndex(int rowIndex) {
//		if (pageSize > 0) {
//			int start = listModel.getPage() * listModel.getPageSize();
//			rowIndex = start + rowIndex;
//		}

		if (gridTab.getCurrentRow() != rowIndex) {//TODO:これは不具合の原因になりそう　girdTabではなくrenderereの方がまだ適切か!?
			gridTab.navigate(rowIndex);
			return true;
		}
		return false;
	}

	/**
	 * Update current row from model
	 */
	public void updateListIndex() {
		if (gridTab == null || !gridTab.isOpen()) return;

//		updateEmptyMessage();

		int rowIndex  = gridTab.getCurrentRow();
//		if (pageSize > 0) {
//			if (paging.getTotalSize() != gridTab.getRowCount())
//				paging.setTotalSize(gridTab.getRowCount());
//			if (paging.getPageCount() > 1 && !gridFooter.isVisible()) {
//				showPagingControl();
//			}
//			int pgIndex = rowIndex >= 0 ? rowIndex % pageSize : 0;
//			int pgNo = rowIndex >= 0 ? (rowIndex - pgIndex) / pageSize : 0;
//			if (listModel.getPage() != pgNo) {
//				listModel.setPage(pgNo);
//				if (renderer.isEditing()) {
//					renderer.stopEditing(false);
//				}
//			} else if (rowIndex == renderer.getCurrentRowIndex()){
//				if (modeless && !renderer.isEditing())
//					echoOnPostSelectedRowChanged();
//				return;
//			} else {
//				if (renderer.isEditing()) {
//					renderer.stopEditing(false);
//					int editingRow = renderer.getCurrentRowIndex();
//					if (editingRow >= 0) {
//						int editingPgIndex = editingRow % pageSize;
//						int editingPgNo = (editingRow - editingPgIndex) / pageSize;
//						if (editingPgNo == pgNo) {
//							listModel.updateComponent(renderer.getCurrentRowIndex() % pageSize);
//						}
//					}
//				}
//			}
//			if (paging.getActivePage() != pgNo) {
//				paging.setActivePage(pgNo);
//			}
//			if (paging.getPageCount() == 1) {
//				hidePagingControl();
//			} else {
//				showPagingControl();
//			}
//			if (rowIndex >= 0 && pgIndex >= 0) {
//				echoOnPostSelectedRowChanged();
//			}
//		} else {
			if (rowIndex >= 0) {
				echoOnPostSelectedRowChanged();
			}
//		}
	}

	private static final String ATTR_ON_POST_SELECTED_ROW_CHANGED = "org.adempiere.webui.adwindow.GridView.onPostSelectedRowChanged";

	/**
	 *
	 */
	protected void echoOnPostSelectedRowChanged() {
//		if (getAttribute(ATTR_ON_POST_SELECTED_ROW_CHANGED) == null) {
//			setAttribute(ATTR_ON_POST_SELECTED_ROW_CHANGED, Boolean.TRUE);
//			Events.echoEvent("onPostSelectedRowChanged", this, null);
//		}
	}

	private boolean saveData()
	{
		try
		{

			Trx.run(new TrxRunnable()
			{
				public void run(String trxName)
				{

					Collection<PO> POs = dirtyModel.values();
					for(PO po :POs)
					{
						po.saveEx(trxName);
					}

				}
			});

			return true;

		}
		catch (Exception e)
		{
			FDialog.error(form.getWindowNo(), form, "Error", e.getLocalizedMessage());
			return false;
		}finally{
			;
		}
	}   //  saveData

}