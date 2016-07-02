package com.teamwizardry.librarianlib.api.gui;

import com.teamwizardry.librarianlib.math.Vec2;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiComponentContainer extends GuiComponent<GuiComponentContainer> {

	public boolean advancedHover = false;
	List<GuiComponent<?>> components = new ArrayList<>();
	List<IGuiDrawable> drawables = new ArrayList<>();
	
	public GuiComponentContainer(int posX, int posY) {
		this(posX, posY, 100, 100);
	}
	public GuiComponentContainer(int posX, int posY, int width, int height) {
		super(posX, posY, width, height);
	}
	
	public void add(GuiComponent<?> component) {
		components.add(component);
		component.setParent(this);
		Collections.sort(components, (a, b) -> Integer.compare(a.zIndex, b.zIndex));
	}
	
	public void add(IGuiDrawable drawable) {
		drawables.add(drawable);
	}
	
	@Override
	public void drawComponent(Vec2 mousePos, float partialTicks) {
//		boolean wasEnabled = ScissorUtil.enable();
//		ScissorUtil.push();
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		GlStateManager.translate(pos.x, pos.y, 0);
		
		/* */
		
		for (GuiComponent<?> component : components) {
			component.draw(component.relativePos(mousePos), partialTicks);
		}
		
		for (IGuiDrawable drawable : drawables) {
			drawable.draw(mousePos, partialTicks);
		}
		
		/* */
		
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
//		ScissorUtil.pop();
//		if(!wasEnabled)
//			ScissorUtil.disable();
	}
	
	@Override
	public boolean isMouseOver(Vec2 mousePos) {
		if(!advancedHover)
			return super.isMouseOver(mousePos);
		
		boolean isOver = false;
		for (GuiComponent component : components) {
			if( component.isMouseOver(component.relativePos(mousePos)) ) {
				isOver = true;
				break;
			}
		}
		
		return isOver;
	}
	
	@Override
	public void keyPressed(char key, int keyCode) {
		super.keyPressed(key, keyCode);
		for (GuiComponent component : components) {
			component.keyPressed(key, keyCode);
		}
	}
	
	@Override
	public void keyReleased(char key, int keyCode) {
		super.keyReleased(key, keyCode);
		for (GuiComponent component : components) {
			component.keyReleased(key, keyCode);
		}
	}
	
	@Override
	public void mouseDown(Vec2 mousePos, EnumMouseButton button) {
		super.mouseDown(mousePos, button);
		for (GuiComponent component : components) {
			component.mouseDown(component.relativePos(mousePos), button);
		}
	}
	
	@Override
	public void mouseDrag(Vec2 mousePos, EnumMouseButton button) {
		super.mouseDrag(mousePos, button);
		for (GuiComponent component : components) {
			component.mouseDrag(component.relativePos(mousePos), button);
		}
	}
	
	@Override
	public void mouseUp(Vec2 mousePos, EnumMouseButton button) {
		super.mouseUp(mousePos, button);
		for (GuiComponent component : components) {
			component.mouseUp(component.relativePos(mousePos), button);
		}
	}

	@Override
	public void mouseWheel(Vec2 mousePos, int direction) {
		super.mouseWheel(mousePos, direction);
		for (GuiComponent component : components) {
			component.mouseWheel(component.relativePos(mousePos), direction);
		}
	}
}
