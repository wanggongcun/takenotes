package com.example.takenotes;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class Wgc_TabListener implements ActionBar.TabListener{

	private Fragment mFragment;
	public Wgc_TabListener(Fragment fragment){
		mFragment = fragment;
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

		ft.add(R.id.frameLayout, mFragment,null);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		ft.remove(mFragment);
	}

}
