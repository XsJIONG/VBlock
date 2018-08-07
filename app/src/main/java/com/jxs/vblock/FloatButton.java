package com.jxs.vblock;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

public class FloatButton extends PopupWindow implements View.OnTouchListener {
	private ImageView Image;
	private Context cx;
	public FloatButton(Context cx) {
		super(cx);
		this.cx=cx;
		InitializeView();
	}
	private void InitializeView() {
		Image=new ImageView(cx);
		Image.setImageDrawable(ContextCompat.getDrawable(cx, R.mipmap.ic_launcher));
		Image.setScaleType(ImageView.ScaleType.FIT_XY);
		Image.setOnTouchListener(this);
		setContentView(Image);
		setBackgroundDrawable(null);
		setWidth(200);
		setHeight(200);
		setTouchable(true);
	}
	private View parent;
	public void showAt(View parent) {
		if (isShowing()) return;
		showAtLocation(this.parent=parent, Gravity.LEFT|Gravity.TOP, 100, 0);
	}
	public void setOnClickListener(View.OnClickListener listener) {
		Image.setOnClickListener(listener);
	}
	private float sx,sy;
	private float ox,oy;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:{
				sx=event.getRawX();
				sy=event.getRawY();
				ox=event.getX();
				oy=event.getY();
				break;
			}
			case MotionEvent.ACTION_MOVE:{

			}
		}
		return false;
	}
}