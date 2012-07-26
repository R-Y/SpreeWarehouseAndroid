package org.genshin.warehouse.racks;

import org.genshin.warehouse.R;
import org.genshin.warehouse.Warehouse;
import org.genshin.warehouse.WarehouseActivity;
import org.genshin.warehouse.stocking.StockingMenuActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class RackDetailsActivity extends Activity {
	private TextView rackName;
	
	private Intent intent;
	private int mode;
	
	private void hookupInterface() {
		rackName = (TextView) findViewById(R.id.rack_name);
		
		mode = Warehouse.ResultCodes.NORMAL.ordinal();
		
		intent = getIntent();
		String modeString = intent.getStringExtra("MODE");
		String name = intent.getStringExtra("SELECT_NAME");
		rackName.setText(name);
		
		if (modeString != null) {
		    if (modeString.equals("CONTAINER_SELECT")) {
		    	mode = Warehouse.ResultCodes.CONTAINER_SELECT.ordinal();
		    	containerSelect(name);
		    }
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rack_details);
		
        Warehouse.setContext(this);
        
		hookupInterface();
	}
	
	public void containerSelect(String selectName) {
		final String name = selectName;
		
		AlertDialog.Builder question = new AlertDialog.Builder(this);
		question.setTitle("このコンテナに登録しますか？");
		question.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(getApplicationContext(), StockingMenuActivity.class);
				intent.putExtra("CONTAINER_NAME", name);
				startActivity(intent);
			}
		});
		question.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		question.show();
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