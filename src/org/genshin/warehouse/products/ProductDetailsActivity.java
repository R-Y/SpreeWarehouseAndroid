package org.genshin.warehouse.products;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.genshin.gsa.ScanSystem;
import org.genshin.gsa.network.NetworkTask;
import org.genshin.spree.SpreeConnector;
import org.genshin.warehouse.R;
import org.genshin.warehouse.WarehouseActivity;
import org.genshin.warehouse.Warehouse.ResultCodes;
import org.genshin.warehouse.products.ProductEditActivity;
import org.genshin.warehouse.stocking.StockingMenuActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

public class ProductDetailsActivity extends Activity {
	SpreeConnector spree;
	
	Bundle extras;
	//private EditText id;
	private TextView name;
	private TextView sku;
	private TextView skuTitle;
	private TextView price;
	private TextView countOnHand;
	private TextView description;
	private TextView permalink;
	private TextView visualCode;
	private ImageSwitcher imageSwitcher;
	private ProductImageViewer imageViewer;
	//private Image image;

	Product product;
	
	private String modeString;
	private String barcodeString;
	
	private void initViewElements() {
		//id = (TextView) findViewById(R.id.product_id);
		name = (TextView) findViewById(R.id.product_name);
		sku = (TextView) findViewById(R.id.product_sku);
		skuTitle = (TextView) findViewById(R.id.product_sku_title);
		price = (TextView) findViewById(R.id.product_price);
		countOnHand = (TextView) findViewById(R.id.product_count_on_hand);
		description = (TextView) findViewById(R.id.product_description);
		permalink = (TextView) findViewById(R.id.product_permalink);
		visualCode = (TextView) findViewById(R.id.product_visualCode);
		imageSwitcher = (ImageSwitcher) findViewById(R.id.product_image_switcher);
	}
	
	private void hookupInterface() {
		imageViewer = new ProductImageViewer(this);
		imageSwitcher.setFactory(imageViewer);
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
        if (product.images.size() == 0)
        	imageSwitcher.setImageResource(R.drawable.spree);
        else
        	imageSwitcher.setImageDrawable(product.images.get(0).data);
        
        Intent intent = getIntent();
        modeString = intent.getStringExtra("MODE");
        barcodeString = intent.getStringExtra("BARCODE");
        if (modeString != null) {
	        if (modeString.equals("UPDATE_PRODUCT_BARCODE")) {
	        	AlertDialog.Builder question = new AlertDialog.Builder(this);
				question.setTitle("この商品に登録しますか？");
				question.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						new registrationBarcode(getApplicationContext(), barcodeString).execute();
					}
				});
				question.setNegativeButton("NO", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				});
				question.show();
	        }
        }
	}

	private void getProductInfo() {
		product = ProductsMenuActivity.getSelectedProduct();
	}

	private void setViewFields() {
		name.setText(product.name);
		
		if (product.sku == "") {
			sku.setVisibility(View.GONE);
			skuTitle.setVisibility(View.GONE);
		} else
			sku.setText(product.sku);
		
		price.setText("" + product.price);
		countOnHand.setText("" + product.countOnHand);
		description.setText(product.description);
		permalink.setText(product.permalink);
		visualCode.setText(product.visualCode);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);
        
        spree = new SpreeConnector(this);

		getProductInfo();
		initViewElements();
		setViewFields();
		hookupInterface();
	}
	
	public static enum menuCodes { stock, destock, registerVisualCode, addProductImage, editProductDetails };
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Resources res = getResources();
        // メニューアイテムを追加します
        menu.add(Menu.NONE, menuCodes.stock.ordinal(), Menu.NONE, res.getString(R.string.stock_in));
        menu.add(Menu.NONE, menuCodes.destock.ordinal(), Menu.NONE, res.getString(R.string.destock));
        if (visualCode.getText() == null || visualCode.getText().equals("") || visualCode.getText().equals("null"))
        	menu.add(Menu.NONE, menuCodes.registerVisualCode.ordinal(), Menu.NONE, res.getString(R.string.register_barcode));
        menu.add(Menu.NONE, menuCodes.addProductImage.ordinal(), Menu.NONE, res.getString(R.string.add_product_image));
        menu.add(Menu.NONE, menuCodes.editProductDetails.ordinal(), Menu.NONE, res.getString(R.string.edit_product_details));
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		  
		//Java can't do this!? WTF!
        /*switch (item.getItemId()) {
        	default:
        		return super.onOptionsItemSelected(item);
        	case registerVisualCode:
            
        		return true;
        }*/
		int id = item.getItemId();

		if (id == menuCodes.registerVisualCode.ordinal()) {
			ScanSystem.initiateScan(this);
        	
			return true;
		} else if (id == menuCodes.editProductDetails.ordinal()) {
			Intent intent = new Intent(this, ProductEditActivity.class);
			startActivity(intent);
		}
        
        return false;
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ResultCodes.SCAN.ordinal()) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                //TODO limit this to bar code types?
                if (ScanSystem.isProductCode(format)) {
                	new Result(getApplicationContext(), contents).execute();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            	Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }
	
	// onActivityResult
	class Result extends NetworkTask {
		String contents;

		public Result(Context ctx, String contents) {
			super(ctx);
			this.contents = contents;
		}
		
		@Override
		protected void process() {
			spree.connector.genericPut("api/products/" + product.permalink + "?product[visual_code]=" + contents);
		}
		
		@Override
		protected void complete() {
			Toast.makeText(getApplicationContext(), R.string.register_barcode, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	// バーコードを商品に登録
	class registrationBarcode extends NetworkTask {
		ArrayList<NameValuePair> pairs;
		String code;

		public registrationBarcode(Context ctx, String code) {
			super(ctx);
			this.code = code;
			this.pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("product[visual_code]", code));
		}
		
		@Override
		protected void process() {
			spree.connector.putWithArgs("api/products/" + product.id + ".json", pairs);
		}
		
		@Override
		protected void complete() {
			modeString = null;
			Toast.makeText(getApplicationContext(), R.string.register_barcode, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(), StockingMenuActivity.class);
			startActivity(intent);
		}		
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
