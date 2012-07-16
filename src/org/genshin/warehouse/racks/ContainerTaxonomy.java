/////////////////////////////////////////////////////////////
// This is for a SINGLE Container Taxonomy

package org.genshin.warehouse.racks;

import java.util.ArrayList;

import org.genshin.warehouse.Warehouse;
import org.genshin.warehouse.orders.OrderLineItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ContainerTaxonomy {
	public int id;
	public String name;
	public String permalink;
	public ContainerTaxon root;
	
	public ArrayList<ContainerTaxon> list;
	
	public boolean child = false;
	
	public ContainerTaxonomy(JSONObject taxonomyJSON) {
		getTaxonomyInfo(taxonomyJSON);
		getRoot(taxonomyJSON);
	}
	
	private void getTaxonomyInfo(JSONObject taxonomyJSON) {
		try {
			this.id = taxonomyJSON.getInt("id");
		} catch (JSONException e) {
			// No ID, but it could contain permalink etc. 
			Log.d("ContainerTaxonomy.getTaxonomyInfo", "Taxonomy did not have a proper ID, setting to -1");
			this.id = -1;
		}
		try {
			this.name = taxonomyJSON.getString("name");
		} catch (JSONException e) { 
			this.name = "";
		}
	}
	
	private void getRoot(JSONObject taxonomyJSON) {
		JSONObject rootJSON = null;
		JSONArray items = null;
		try {
			rootJSON = taxonomyJSON.getJSONObject("root");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rootJSON = null;
		}
		
		if (rootJSON != null)
			this.root = new ContainerTaxon(rootJSON);
		
		if (rootJSON != null) {
			try {
				items = rootJSON.getJSONArray("container_taxons");
			} catch (JSONException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		
		if (items != null){
			try {			
				for (int i = 0; i < items.length(); i++) {
					JSONObject item = items.getJSONObject(i).getJSONObject("container_taxon");
					child = true;
				}
			} catch (JSONException e) {
				child = false;
				e.printStackTrace();
			}
		}
	}
	
	//TODO will probably just be a tree - prune this?
	/*private void getTaxonTree(JSONObject taxonomyJSON) {
		JSONObject innerTaxonomyJSON = null;
		try {
			innerTaxonomyJSON = taxonomyJSON.getJSONObject("container_taxonomy");
		} catch (JSONException e) {
			//no inner taxonomies
			innerTaxonomyJSON = null;
		}
		
		if (innerTaxonomyJSON != null) {
			//process inner taxonomy
			//taxonomies.add(new ContainerTaxonomy(innerTaxonomyJSON));
		}
	}*/
	
	// overload
	public ContainerTaxonomy(String selectId) {
		JSONObject taxonomyJSON = Warehouse.Spree().connector.getJSONObject("/api/container_taxonomies/" + selectId + ".json");
		list = new ArrayList<ContainerTaxon>();
		JSONObject innerTaxonomyJSON = null;
		JSONObject rootJSON = null;
		JSONArray items = null;
		
		try {
			innerTaxonomyJSON = taxonomyJSON.getJSONObject("container_taxonomy");
		} catch (JSONException e) {
			//no inner taxonomies
			e.printStackTrace();
			innerTaxonomyJSON = null;
		}
		
		if (innerTaxonomyJSON != null) {
			try {
				rootJSON = innerTaxonomyJSON.getJSONObject("root");		
			} catch (JSONException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
				rootJSON = null;
			}
		}
		
		if (rootJSON != null) {
			try {
				items = rootJSON.getJSONArray("container_taxons");
			} catch (JSONException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		
		if (items != null){
			try {			
				for (int i = 0; i < items.length(); i++) {
					JSONObject item = items.getJSONObject(i).getJSONObject("container_taxon");
					ContainerTaxon listItem = new ContainerTaxon(item);	
					list.add(listItem);
					child = true;
				}
			} catch (JSONException e) {
				list.add(null);
				child = false;
				e.printStackTrace();
			}
		}
	}
}
