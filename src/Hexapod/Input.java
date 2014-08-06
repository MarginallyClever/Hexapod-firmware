package Hexapod;

import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;

public class Input {
	public static enum ButtonState {
		OFF,
		PRESSED,
		ON,
		RELEASED,
	}
	
	public static final int MAX_MICE              =(4);
	public static final int MAX_JOYSTICKS         =(2);
	public static final int NUM_BUTTONS_PER_MOUSE = MouseEvent.BUTTON_COUNT;
	public static final int NUM_BUTTONS_PER_KB    = 256;
	
	public static final int NUM_AXIES_PER_MOUSE   =(4);
	public static final int NUM_BUTTONS_PER_JOY   =(14);
	public static final int NUM_AXIES_PER_JOY     =(10);
	public static final int NUM_HAT_PER_JOY       =(4);
	public static final int NUM_MOUSE_BUTTONS     =(NUM_BUTTONS_PER_MOUSE*MAX_MICE);
	public static final int NUM_MOUSE_AXIES       =(NUM_AXIES_PER_MOUSE*MAX_MICE);
	public static final int NUM_JOY_BUTTONS       =(NUM_BUTTONS_PER_JOY*MAX_JOYSTICKS);
	public static final int NUM_JOY_AXIES         =(NUM_AXIES_PER_JOY*MAX_JOYSTICKS);
	public static final int NUM_JOY_HATS          =(NUM_HAT_PER_JOY*MAX_JOYSTICKS);

	public static final int FIRST_KB_BUTTON       =0;
	public static final int FIRST_MOUSE_BUTTON    =(NUM_BUTTONS_PER_KB);
	public static final int FIRST_MOUSE_AXIS      =(FIRST_MOUSE_BUTTON+NUM_MOUSE_BUTTONS);

	public static final int FIRST_JOY_BUTTON      =(FIRST_MOUSE_AXIS  +NUM_MOUSE_AXIES  );
	public static final int FIRST_JOY_AXIS        =(FIRST_JOY_BUTTON  +NUM_JOY_BUTTONS  );
	public static final int FIRST_JOY_HAT         =(FIRST_JOY_AXIS    +NUM_JOY_AXIES    );
	public static final int TOTAL_KEYS            =(FIRST_JOY_HAT     +NUM_JOY_HATS     );

	class InputElement {
		float value;
		float rangeMin;
		float rangeMax;
		float rangeHalf;
	};
		
	class InputContextKey {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		float rangeMin;
		float rangeMax;
	};

	class InputContext {
		Map<String,InputContextKey> keys = new HashMap<String,InputContextKey>();
	};
	
	class InputData {
		InputElement [] items_new = new InputElement[TOTAL_KEYS];  // the current state
		InputElement [] items_old = new InputElement[TOTAL_KEYS];  // the previous state
		boolean hasFocus;   // is the window in focus
		
		InputData() {
			for(int i=0;i<TOTAL_KEYS;++i) {
				items_new[i] = new InputElement();
				items_old[i] = new InputElement();
			}
		}
	};

	protected Map<String,Integer> keyNames = new HashMap<String,Integer>();
	protected Map<String,InputContext> inputContextList = new HashMap<String,InputContext>();
	protected ArrayList<Integer> unicodeQueue = new ArrayList<Integer>();
	protected InputData data = new InputData();
	protected float mouse_old_x;
	protected float mouse_old_y;
	
	private static Input __singleton = null;
	
	
	protected Input() {
		System.out.println("Starting input...");

		// keyboard events
		for (Field f : KeyEvent.class.getDeclaredFields()) {
            try {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && (f.getType() == int.class || f.getType() == short.class) && f.getName().startsWith("VK") ) {
                    f.setAccessible(true);
                    String name=f.getName();
                    int id = f.getInt(null); 
                    keyNames.put(name,Input.FIRST_KB_BUTTON+id);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }

		// mouse events
		keyNames.put("mouse_left",   FIRST_MOUSE_BUTTON+0);
		keyNames.put("mouse_middle", FIRST_MOUSE_BUTTON+1);
		keyNames.put("mouse_right",  FIRST_MOUSE_BUTTON+2);

		keyNames.put("mouse_xpos", FIRST_MOUSE_AXIS+0);
		keyNames.put("mouse_xneg", FIRST_MOUSE_AXIS+1);
		keyNames.put("mouse_ypos", FIRST_MOUSE_AXIS+2);
		keyNames.put("mouse_yneg", FIRST_MOUSE_AXIS+3);
		keyNames.put("mouse_wpos", FIRST_MOUSE_AXIS+4);
		keyNames.put("mouse_wneg", FIRST_MOUSE_AXIS+5);

		// axis data
		for(int i=0;i<TOTAL_KEYS;++i) {
		    data.items_new[i].rangeMax=1.0f;
		    data.items_new[i].rangeMin=0.0f;
		    data.items_new[i].rangeHalf=0.5f;
		    data.items_old[i].rangeMax=1.0f;
		    data.items_old[i].rangeMin=0.0f;
		    data.items_old[i].rangeHalf=0.5f;
		}
/*
		for(int i=0;i<NUM_MOUSE_AXIES;++i) {
			data.items_new[FIRST_MOUSE_AXIS+i].rangeMax =2.0f;
			data.items_new[FIRST_MOUSE_AXIS+i].rangeMin = 0.0f;
			data.items_new[FIRST_MOUSE_AXIS+i].rangeHalf=1.0f;
			data.items_old[FIRST_MOUSE_AXIS+i].rangeMax =2.0f;
			data.items_old[FIRST_MOUSE_AXIS+i].rangeMin = 0.0f;
			data.items_old[FIRST_MOUSE_AXIS+i].rangeHalf=1.0f;
		}*/

		System.out.println("Ready...");
	}
	
	public static Input GetSingleton() {
		if(__singleton==null) __singleton = new Input();
		return __singleton;
	}
	
	public InputContextKey getInputContextKey(String context_name,String action_name) {
		if(!inputContextList.containsKey(context_name)) {
			inputContextList.put(context_name,new InputContext());
		}
		InputContext context = inputContextList.get(context_name);
		if(!context.keys.containsKey(action_name)) {
			context.keys.put(action_name,new InputContextKey());
		}
		InputContextKey key = context.keys.get(action_name);
		 
		return key;
	}
	
	protected ButtonState getInputState(int index) {
		boolean b0 = data.items_old[index].value > data.items_old[index].rangeHalf;
		boolean b1 = data.items_new[index].value > data.items_new[index].rangeHalf;

		if(b0==false) {
			if(b1==false) {
				return ButtonState.OFF;
			} else {
				return ButtonState.PRESSED;
			}
		} else {
			if(b1==false) {
				return ButtonState.RELEASED;
			} else {
				return ButtonState.ON;
			}
		}
	}
	
	public ButtonState GetButtonState(String context_name,String action_name) {
		InputContextKey k = getInputContextKey(context_name,action_name);

		// order of importance (from least to most): OFF,RELEASED,PRESSED,ON
		// so if two buttons do the same thing, releasing one and holding the other
		// will only produce the one ON message.  This may cause rare glitches.  Hello, future debugger!
		ButtonState state=Input.ButtonState.OFF;
		int i;
		for(i=0;i<k.ids.size();++i) {
			int input_id = k.ids.get(i);
			if( input_id == Input.FIRST_KB_BUTTON+KeyEvent.VK_SPACE) {
				assert(false);
			}
			ButtonState x=getInputState(input_id);
		    if(x==Input.ButtonState.ON) {
		    	//System.out.println(context_name+" "+action_name+" ON");
		    	return Input.ButtonState.ON;
		    }
		    
		    if(state==ButtonState.OFF) {
		        if(x==ButtonState.RELEASED) {
			    	//System.out.println(context_name+" "+action_name+" RELEASED");
			    	state=ButtonState.RELEASED;
		        }
		        else if(x==ButtonState.PRESSED) {
			    	//System.out.println(context_name+" "+action_name+" PRESSED");
			    	state=ButtonState.PRESSED;
		        }
		    }/*
		    if(state==Input.ButtonState.RELEASED && x==Input.ButtonState.PRESSED) {
		    	System.out.println(context_name+" "+action_name+" RELEASED");
		    	state=Input.ButtonState.RELEASED;
		    }
		    if(state==Input.ButtonState.OFF && x==Input.ButtonState.RELEASED) {
		    	System.out.println(context_name+" "+action_name+" PRESSED");
		    	state=Input.ButtonState.PRESSED;
		    }*/
		}

		return state;
	}

	public float GetAxisState(String context_name,String action_name) {
		InputContextKey k = getInputContextKey(context_name,action_name);

		float biggest=(0);
		int i;
		for(i=0;i<k.ids.size();++i) {
			InputElement ie = data.items_old[k.ids.get(i)];
		    float f=ie.value/(ie.rangeMax-ie.rangeMin);  //0...1 input range
		    //f=(f-key->second.cutoffMin)/(key->second.cutoffMax-key->second.cutoffMin); //0...1 adjusted
		    if(f>1.0f)
		       f=1.0f;
		    if(biggest<f)
		      biggest=f;
		  }
		  return biggest*(k.rangeMax-k.rangeMin)+k.rangeMin; 
	}

	public void gainFocus() {
	}
	public void loseFocus() {
    	data.hasFocus=false;
	}
    public void mouseEntered(MouseEvent e) {
    	gainFocus();
    	mouse_old_x = e.getX();
    	mouse_old_y = e.getY();
    }
    public void mouseExited(MouseEvent e) {
    	loseFocus();
    }

    protected void addAxisAction(int key_code,float v) {
		data.items_old[key_code].value = data.items_new[key_code].value;
		data.items_new[key_code].value = v;
    }
    protected void addKeyAction(int key_code,int pressed) {
    	//System.out.println(key_code+" "+pressed);
    	data.items_old[key_code].value = data.items_new[key_code].value;
		data.items_new[key_code].value = pressed;
    }
    
    public void mouseDragged(MouseEvent e) {
    	mouseMoved(e);
    }
    public void mouseMoved(MouseEvent e) {
    	// @TODO not built to handle multiple mice
    	float x = e.getX();
    	float y = e.getY();
    	float dx = x - mouse_old_x;
    	float dy = y - mouse_old_y;
    	mouse_old_x = x;
    	mouse_old_y = y;

    	data.items_new[FIRST_MOUSE_AXIS+0].value += dx>0? dx:0;
    	data.items_new[FIRST_MOUSE_AXIS+1].value += dx<0?-dx:0;
    	data.items_new[FIRST_MOUSE_AXIS+2].value += dy>0? dy:0;
    	data.items_new[FIRST_MOUSE_AXIS+3].value += dy<0?-dy:0;
    }
    
    public void mouseWheelMoved(MouseWheelEvent  e) {
    	int dz=e.getWheelRotation();
    	data.items_new[FIRST_MOUSE_AXIS+4].value += dz>0? dz:0;
    	data.items_new[FIRST_MOUSE_AXIS+5].value += dz<0? dz:0;
    }
    
    public void mouseClicked(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {
    	addKeyAction(Input.FIRST_MOUSE_BUTTON+e.getButton()-1,1);
    }
    public void mouseReleased(MouseEvent e) {
    	addKeyAction(Input.FIRST_MOUSE_BUTTON+e.getButton()-1,0);
    }    
    public void keyPressed(KeyEvent e) {
    	addKeyAction(Input.FIRST_KB_BUTTON+e.getKeyCode(),1);
   	}
	public void keyReleased(KeyEvent e) {
		addKeyAction(Input.FIRST_KB_BUTTON+e.getKeyCode(),0);
    }

	public void AddContext(String context_name,String action_name,String input_name) {
		AddContext(context_name,action_name,input_name,0,1);
	}
	public void AddContext(String context_name, String action_name, String input_name, float rangeMin, float rangeMax ) {
		String [] names = input_name.split(",");
		for(int i=0;i<names.length;++i) {
			if(!keyNames.containsKey(names[i])) {
				System.out.println("invalid key name '"+names[i]+"'");
				continue;
			}
			
			InputContextKey key = getInputContextKey(context_name,action_name);
			key.ids.add(keyNames.get(names[i]));
			key.rangeMax=rangeMax;
			key.rangeMin=rangeMin;
		}
	}
	
	public void update() {
		// keyboards are polite enough to tell us when the action ends.
		// mice say nothing when they stop moving.
		for(int i=0;i<NUM_BUTTONS_PER_KB;++i) {
			data.items_old[FIRST_KB_BUTTON+i].value = data.items_new[FIRST_KB_BUTTON+i].value;
		}
		for(int i=0;i<NUM_BUTTONS_PER_MOUSE;++i) {
			data.items_old[FIRST_MOUSE_BUTTON+i].value = data.items_new[FIRST_MOUSE_BUTTON+i].value;
		}
		
		// keyboards are polite enough to tell us when the action ends.
		// mice say nothing when they stop moving.
		for(int i=0;i<NUM_MOUSE_AXIES;++i) {
			addAxisAction(FIRST_MOUSE_AXIS+i,0);
		}
	}
}
