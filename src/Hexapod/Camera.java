package Hexapod;
import javax.vecmath.Vector3f;
import javax.media.opengl.GL2;


public class Camera {
	// position of camera
	public Vector3f position = new Vector3f();
	// orientation
	public Vector3f forward = new Vector3f(0,1,0);
	public Vector3f up = new Vector3f(0,0,1);
	public Vector3f right = new Vector3f(1,0,0);
	// angles
	public float pan, tilt;

	public int pan_dir=0;
	public int tilt_dir=0;
	public int move_ud=0;
	public int move_lr=0;
	public int move_fb=0;
	
	
	public Camera() {
		position.set(0,100,-20);
		pan=0;
		tilt=80;

		Input.GetSingleton().AddContext("camera", "active", "mouse_right");
		
		Input.GetSingleton().AddContext("camera", "look_pan_up", "mouse_xpos");
		Input.GetSingleton().AddContext("camera", "look_pan_down", "mouse_xneg");
		Input.GetSingleton().AddContext("camera", "look_tilt_up", "mouse_ypos");
		Input.GetSingleton().AddContext("camera", "look_tilt_down", "mouse_yneg");

		Input.GetSingleton().AddContext("camera", "up", "VK_Q");
		Input.GetSingleton().AddContext("camera", "down", "VK_E");
		Input.GetSingleton().AddContext("camera", "left", "VK_A");
		Input.GetSingleton().AddContext("camera", "right", "VK_D");
		Input.GetSingleton().AddContext("camera", "forward", "VK_W");
		Input.GetSingleton().AddContext("camera", "back", "VK_S");
	}

	public void update(float dt) {
		//if(Input.GetSingleton().GetButtonState("camera", "active") != Input.ButtonState.ON) return;
		
/*
  		pan_dir = (Input.GetSingleton().GetAxisState("camera", "look_pan_up") - Input.GetSingleton().GetAxisState("camera", "look_pan_down"));
		tilt_dir = (Input.GetSingleton().GetAxisState("camera", "look_tilt_up") - Input.GetSingleton().GetAxisState("camera", "look_tilt_down"));
		move_ud = ( Input.GetSingleton().GetButtonState("camera", "up")==Input.ButtonState.ON ) ? 1 : 0 +
				  ( Input.GetSingleton().GetButtonState("camera", "down")==Input.ButtonState.ON ) ? -1 : 0;
		move_lr = ( Input.GetSingleton().GetButtonState("camera", "left")==Input.ButtonState.ON ) ? 1 : 0 +
				  ( Input.GetSingleton().GetButtonState("camera", "right")==Input.ButtonState.ON ) ? -1 : 0;
		move_fb = ( Input.GetSingleton().GetButtonState("camera", "forward")==Input.ButtonState.ON ) ? 1 : 0 +
				  ( Input.GetSingleton().GetButtonState("camera", "back")==Input.ButtonState.ON ) ? -1 : 0;
		*/
		
		pan += pan_dir*10*dt;
		tilt += tilt_dir*10*dt;
		
		if(tilt < 1) tilt=1;
		if(tilt > 179) tilt= 179;

		// calculate new vectors for translation based on pan/tilt angles
		forward.y = (float)Math.sin((-pan-90) * Math.PI/180.0) * (float)Math.cos((90-tilt) * Math.PI/180.0);
		forward.x = (float)Math.cos((-pan-90) * Math.PI/180.0) * (float)Math.cos((90-tilt) * Math.PI/180.0);
		forward.z =                                              (float)Math.sin((90-tilt) * Math.PI/180.0);
		
		up.set(0,0,1);

		right.cross(forward, up);
		right.normalize();
		up.cross(right, forward);
		up.normalize();

		// move the camera
		Vector3f temp = new Vector3f();
		Vector3f direction = new Vector3f(0,0,0);
		float vel = 10f * dt;
		boolean changed = false;
		
		// which way do we want to move?
		float delta = 1;
		
		if(move_fb!=0) {
			// forward/back
			temp.set(forward);
			temp.scale(delta * move_fb);
			direction.add(temp);
			changed = true;
		}
		if(move_lr!=0) {
			// strafe left/right
			temp.set(right);
			temp.scale(-delta * move_lr);
			direction.add(temp);
			changed = true;
		}
		if(move_ud!=0) {
			// strafe up/down
			temp.set(up);
			temp.scale(-delta * move_ud);
			direction.add(temp);
			changed = true;
		}
		
		if(changed) {
			direction.normalize();
			direction.scale(vel);
			position.add(direction);
		}
	}

	
	
	void render(GL2 gl2) {
		// move camera
		gl2.glRotatef(tilt, -1, 0, 0);
		gl2.glRotatef(pan,0,0,1);
		gl2.glTranslatef(position.x,position.y,position.z);
	}
}
