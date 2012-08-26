package org.genshin.warehouse.racks;

import java.util.ArrayList;

import org.genshin.gsa.network.NetworkTask;
import org.genshin.warehouse.R;
import org.genshin.warehouse.Warehouse;
import org.genshin.warehouse.WarehouseActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

public class RacksMenuActivity extends Activity {
	private ExpandableListView racksRootList;
    private RacksMenuAdapter adapter;
    
    private Intent intent;
    private TextView selectRack;
    
    private static int mode;
    private String selectId;
    
	private WarehouseDivisions warehouses;
    private ArrayList<RacksListData> warehouseRoots;
    private ArrayList<ArrayList<RacksListData>> containerTaxonomyNodes;
    
    private ContainerTaxonomy selectContainer;
    
	private void hookupInterface() {
		
		racksRootList = (ExpandableListView) findViewById(R.id.racks_root_list);
		selectRack = (TextView) findViewById(R.id.racks_select);
		
		intent = getIntent();
		mode = Warehouse.ResultCodes.NORMAL.ordinal();
		String modeString = intent.getStringExtra("MODE");
		String expand = intent.getStringExtra("EXPAND");
		String selectName = null;

		// モード判別
		if (modeString != null) {
			if (modeString.equals("CONTAINER_SELECT"))
				mode = Warehouse.ResultCodes.CONTAINER_SELECT.ordinal();
		} else
			mode = Warehouse.ResultCodes.NORMAL.ordinal();
		
		// 表示判別
		if (expand != null) {	
			if (expand.equals("MORE")) {
				selectName = intent.getStringExtra("SELECT_NAME");
				selectId = intent.getStringExtra("ID");
				selectRack.setText(selectName);
			} 
		} else {
			selectId = null;
			selectRack.setText("/");
		}

		if (expand != null) {
			new showExpandRacksMenu(this, selectId).execute();
		} else
			new showRacksMenuList(this).execute();

        racksRootList.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				RacksMenuAdapter adapter = (RacksMenuAdapter)parent.getExpandableListAdapter();
				String selectId = adapter.getChild(groupPosition, childPosition).id;
				String text = adapter.getChild(groupPosition, childPosition).name;

				if (adapter.getChild(groupPosition, childPosition).icon) {
					intent = new Intent(getApplicationContext(), RacksMenuActivity.class);
					intent.putExtra("EXPAND", "MORE");
				} else 
					intent = new Intent(getApplicationContext(), RackDetailsActivity.class);
				intent.putExtra("SELECT_NAME", text);
				intent.putExtra("ID", selectId);
				
				if (mode == Warehouse.ResultCodes.CONTAINER_SELECT.ordinal())
					intent.putExtra("MODE", "CONTAINER_SELECT");

				startActivity(intent);

				return false;
			}
		});

        racksRootList.setOnGroupClickListener(new OnGroupClickListener() {		
			public boolean onGroupClick(ExpandableListView parent, View v,
													int groupPosition, long id) {
				if (adapter.getChild(groupPosition, 0).id == null) {
					// 子要素が空の場合の処理
					//String selectId = adapter.getGroup(groupPosition).id;
					String selectId = "0";
					String text = adapter.getGroup(groupPosition).name;
					intent = new Intent(getApplicationContext(), RackDetailsActivity.class);
					intent.putExtra("SELECT_NAME", text);
					intent.putExtra("ID", selectId);
					
					if (mode == Warehouse.ResultCodes.CONTAINER_SELECT.ordinal())
						intent.putExtra("MODE", "CONTAINER_SELECT");

					startActivity(intent);
					return true;
				}
				else
					return false;
			}		
		});
        
        //if (adapter.getGroupCount() == 0) {
        // 	finishActivity(Warehouse.ResultCodes.CONTAINER_SELECT.ordinal());
        //}
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.racks);
		
        Warehouse.setContext(this);
        Warehouse.Warehouses();
        
		hookupInterface();
	}
	
	class showRacksMenuList extends NetworkTask {
		private Context ctx;

		public showRacksMenuList(Context ctx) {
			super(ctx);
			this.ctx = ctx;
		}

		@Override
		protected void process() {
			warehouses = Warehouse.Warehouses();
			warehouses.getWarehouses();
			putJsonData();
		}
		
		protected void complete() {
			racksRootList = (ExpandableListView) findViewById(R.id.racks_root_list);
			adapter = new RacksMenuAdapter(
					Warehouse.getContext(), warehouseRoots, containerTaxonomyNodes);
			Log.v("test", "" + adapter);
			racksRootList.setAdapter(adapter);
	        racksRootList.setGroupIndicator(null);
		}
	}
	
	class showExpandRacksMenu extends showRacksMenuList {
		private Context ctx;
		private String selectId;
		
		public showExpandRacksMenu(Context ctx, String selectId) {
			super(ctx);
			this.ctx = ctx;
			this.selectId = selectId;
		}

		@Override
		protected void process() {
			warehouses = Warehouse.Warehouses();
			warehouses.getWarehouses();
			selectContainer = new ContainerTaxonomy(selectId);
			putJsonDataExpand(selectId);
		}
	}
	
	// 最初の表示データ
	public void putJsonData() {
		
        warehouseRoots = new ArrayList<RacksListData>();
        containerTaxonomyNodes = new ArrayList<ArrayList<RacksListData>>();

		for (int i = 0; i < warehouses.count; i++) {
			RacksListData warehouseDivisionMap = new RacksListData();
			warehouseDivisionMap.name = (warehouses.divisions.get(i).name);
			warehouseDivisionMap.id = ("" + warehouses.divisions.get(i).id);

			ArrayList<RacksListData> taxonomyNodeList = new ArrayList<RacksListData>();
			if (warehouses.divisions.get(i).containers.size() == 0) {
				RacksListData taxonomyNode = new RacksListData();
				taxonomyNode.group = null;
				taxonomyNode.name = null;
				taxonomyNode.id = null;
				taxonomyNodeList.add(taxonomyNode);
			} else {
				for (int j = 0; j < warehouses.divisions.get(i).containers.size(); j++) {
					RacksListData taxonomyNode = new RacksListData();
					taxonomyNode.group = (warehouses.divisions.get(i).name);
					taxonomyNode.name = (warehouses.divisions.get(i).containers.get(j).name);
					taxonomyNode.id = ("" + warehouses.divisions.get(i).containers.get(j).id);
					taxonomyNode.permalink = (warehouses.divisions.get(i).containers.get(j).permalink);
					taxonomyNodeList.add(taxonomyNode);
					
					if (warehouses.divisions.get(i).containers.taxonomies.get(j).child)
						taxonomyNode.icon = true;
				}
			}
			containerTaxonomyNodes.add(taxonomyNodeList);
			warehouseRoots.add(warehouseDivisionMap);
		}
	}
	
	// 次の表示データ
	public void putJsonDataExpand(String selectId) {
		
		warehouseRoots = new ArrayList<RacksListData>();
        containerTaxonomyNodes = new ArrayList<ArrayList<RacksListData>>();
	
    	for (int i = 0; i < selectContainer.list.size(); i++) {
			RacksListData warehouseDivisionMap = new RacksListData();
			warehouseDivisionMap.name = (selectContainer.list.get(i).name);
			warehouseDivisionMap.id = ("" + selectContainer.list.get(i).id);
			warehouseDivisionMap.permalink = (selectContainer.list.get(i).permalink);
			
			ArrayList<RacksListData> taxonomyNodeList = new ArrayList<RacksListData>();

			// 子要素がない場合はnull挿入・・・とりあえず今は参考データがないので全部null
			RacksListData taxonomyNode = new RacksListData();
			taxonomyNode.group = null;
			taxonomyNode.name = null;
			taxonomyNode.id = null;
			taxonomyNodeList.add(taxonomyNode);

			containerTaxonomyNodes.add(taxonomyNodeList);
			warehouseRoots.add(warehouseDivisionMap);
		}
    }

	public static enum menuCodes { newRegister };
	
	// メニュー
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Resources res = getResources();
        // メニューアイテムを追加する
        menu.add(Menu.NONE, menuCodes.newRegister.ordinal(), Menu.NONE, res.getString(R.string.new_register));
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == menuCodes.newRegister.ordinal()) {
			Intent intent = new Intent(this, RackEditActivity.class);
			intent.putExtra("IS_NEW", true);
            startActivity(intent);
        	
			return true;
		}
        
        return false;
    }
	
	// 長押しで最初の画面へ
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) 
	    {
	    	startActivity(new Intent(this, WarehouseActivity.class));
	        return true;
	    }
	    return super.onKeyLongPress(keyCode, event);
	}
}
