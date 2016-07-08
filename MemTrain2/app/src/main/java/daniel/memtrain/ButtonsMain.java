package daniel.memtrain;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//THIS IS THE LAYOUT TO OUR MAIN BOARD
public class ButtonsMain extends View implements MemTrain.Listener {

	// measurements
	private static final int BUTTON_GRID_SIZE = 2; //we use a 2x2 grid for 4 buttons total
	private static final float BUTTON_PADDING = 0.01f;
	private float buttonCellSize;
	private float scale;
	
	// drawing tools. we use bitmap to manage our animation states
	private Bitmap redOn;
	private Bitmap redOff;
	private Bitmap blueOn;
	private Bitmap blueOff;
	private Bitmap greenOn;
	private Bitmap greenOff;
	private Bitmap purpleOn;
	private Bitmap purpleOff;
	private Bitmap buttonPressed;
	private Bitmap buttonIdle;
	
	// model
	private MemTrain model;

	//we initialize using super to make sure we don't break parent class logic
	public ButtonsMain(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ButtonsMain(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ButtonsMain(Context context) {
		super(context);
		init();
	}

	private void init() {
		initDrawingInstruments();
	}

	private void initDrawingInstruments() { //we assign our pictures to our different button states
		Resources resources = getContext().getResources();
		
		redOn = BitmapFactory.decodeResource(resources, R.drawable.redshape_on);
		redOff = BitmapFactory.decodeResource(resources, R.drawable.redshape_off);
		blueOn = BitmapFactory.decodeResource(resources, R.drawable.blueshape_on);
		blueOff = BitmapFactory.decodeResource(resources, R.drawable.blueshape_off);
		greenOn = BitmapFactory.decodeResource(resources, R.drawable.greenshape_on);
		greenOff = BitmapFactory.decodeResource(resources, R.drawable.greenshape_off);
		purpleOn = BitmapFactory.decodeResource(resources, R.drawable.purpleshape_on);
		purpleOff = BitmapFactory.decodeResource(resources, R.drawable.purpleshape_off);
	}	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		scale = canvas.getClipBounds().width();
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);
		drawButtons(canvas);
		canvas.restore();
	}

	private void drawButtons(Canvas canvas) {
		for (int row = 0; row < BUTTON_GRID_SIZE; ++row) {
			for (int col = 0; col < BUTTON_GRID_SIZE; ++col) {
				drawButton(canvas, row, col);
			}
		}
	}

	private void drawButton(Canvas canvas, int row, int col) {
		buttonCellSize = 1.0f / BUTTON_GRID_SIZE;
		
		float buttonCellTop = row * buttonCellSize;
		float buttonCellLeft = col * buttonCellSize;
		float buttonTop = buttonCellTop + BUTTON_PADDING;
		float buttonLeft = buttonCellLeft + BUTTON_PADDING;
		float buttonSize = (buttonCellSize - BUTTON_PADDING * 2); 
		
		Bitmap bitmap = getBitmapForButton(row, col);

		float pixelSize = canvas.getClipBounds().width();
		float bitmapScaleX = (pixelSize / bitmap.getWidth()) * buttonSize;
		float bitmapScaleY = (pixelSize / bitmap.getHeight()) * buttonSize;
		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(bitmapScaleX, bitmapScaleY);
		canvas.drawBitmap(bitmap, buttonLeft / bitmapScaleX, buttonTop / bitmapScaleY, null);
		canvas.restore();
	}

	private Bitmap getBitmapForButton(int row, int col) {
		boolean pressed = model.isButtonPressed(getButtonIndex(row, col));
		int button_number = getButtonIndex(row, col);
		switch (button_number) {
		case 0:
			return pressed ?
					greenOn : greenOff;
		case 1:
			return pressed ?
					redOn : redOff;
		case 2:
			return pressed ?
					purpleOn : purpleOff;
		case 3:
			return pressed ?
					blueOn : blueOff;
		default: 
			return pressed ?
					buttonPressed : buttonIdle;
		}
	}

	private int getButtonIndex(int row, int col) {
		return row * BUTTON_GRID_SIZE + col;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		setMeasuredDimension(chosenDimension, chosenDimension);
	}

	public void setMemTrain(MemTrain model) {
		if (model != null) {
			model.removeListener(this);
		}
		this.model = model;
		if (model != null) {
			model.addListener(this);
		}
	}
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return 300;
		} 
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int buttonIndex = -1;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			buttonIndex = getButtonByCoords(event.getX(), event.getY());
			if (buttonIndex != -1) {
				model.pressButton(buttonIndex);
			}
			return true;
		case MotionEvent.ACTION_UP:
			buttonIndex = getButtonByCoords(event.getX(), event.getY());
			if (buttonIndex != -1) {
				model.releaseButton(buttonIndex);
			}
			model.releaseAllButtons();
			return true;
		}
		return false;
	}

	private int getButtonByCoords(float x, float y) {
		float scaledX = x / scale;
		float scaledY = y / scale;

		float buttonCellX = (float)Math.floor(scaledX / buttonCellSize);
		float buttonCellY = (float)Math.floor(scaledY / buttonCellSize);

		return getButtonIndex((int) buttonCellY, (int) buttonCellX);
	}

	//invalidate tells our system to redraw using onDraw upon a view change
	public void buttonStateChanged(int index) { invalidate();}

	public void multipleButtonStateChanged() {
		invalidate();
	}
}
