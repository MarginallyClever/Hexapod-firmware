package Hexapod;
import javax.vecmath.Vector3f;
import javax.media.opengl.GL2;


public class Camera {
	// position of camera
	Vector3f position = new Vector3f();
	// orientation
	Vector3f forward = new Vector3f(0,1,0);
	Vector3f up = new Vector3f(0,0,1);
	Vector3f right = new Vector3f(1,0,0);
	// angles
	float pan, tilt;
	// flight speed
	float delta=1;

	
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
		if(Input.GetSingleton().GetButtonState("camera", "active") != Input.ButtonState.ON) return;
		
		pan  += (Input.GetSingleton().GetAxisState("camera", "look_pan_up") - Input.GetSingleton().GetAxisState("camera", "look_pan_down"));
		tilt += (Input.GetSingleton().GetAxisState("camera", "look_tilt_up") - Input.GetSingleton().GetAxisState("camera", "look_tilt_down"));
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
		float vel = 0.75f;
		boolean changed = false;
		
		// which way do we want to move?
		
		boolean qDown = Input.GetSingleton().GetButtonState("camera", "up")==Input.ButtonState.ON;
		boolean eDown = Input.GetSingleton().GetButtonState("camera", "down")==Input.ButtonState.ON;
		boolean aDown = Input.GetSingleton().GetButtonState("camera", "left")==Input.ButtonState.ON;
		boolean dDown = Input.GetSingleton().GetButtonState("camera", "right")==Input.ButtonState.ON;
		boolean wDown = Input.GetSingleton().GetButtonState("camera", "forward")==Input.ButtonState.ON;
		boolean sDown = Input.GetSingleton().GetButtonState("camera", "back")==Input.ButtonState.ON;
		
		if(wDown != sDown) {
			if (wDown) {
				// forward
				temp.set(forward);
				temp.scale(delta);
				direction.add(temp);
				changed = true;
			}
			if (sDown) {
				// back
				temp.set(forward);
				temp.scale(-delta);
				direction.add(temp);
				changed = true;
			}
		}
		if(aDown != dDown) {
			if (aDown) {
				// strafe left
				temp.set(right);
				temp.scale(-delta);
				direction.add(temp);
				changed = true;
			}
			if (dDown) {
				// strafe right
				temp.set(right);
				temp.scale(delta);
				direction.add(temp);
				changed = true;
			}
		}
		if(qDown != eDown) {
			if (qDown) {
				// strafe left
				temp.set(up);
				temp.scale(delta);
				direction.add(temp);
				changed = true;
			}
			if (eDown) {
				// strafe right
				temp.set(up);
				temp.scale(-delta);
				direction.add(temp);
				changed = true;
			}
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
