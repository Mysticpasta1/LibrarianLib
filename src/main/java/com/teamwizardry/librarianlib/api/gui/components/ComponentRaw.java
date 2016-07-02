package com.teamwizardry.librarianlib.api.gui.components;

import com.teamwizardry.librarianlib.api.gui.GuiComponent;
import com.teamwizardry.librarianlib.math.Vec2;

import java.util.function.Consumer;

public class ComponentRaw extends GuiComponent<ComponentRaw> {
	
	public Consumer<ComponentRaw> func;
	
	public ComponentRaw(int posX, int posY, Consumer<ComponentRaw> func) {
		super(posX, posY);
		this.func = func;
	}

	public ComponentRaw(int posX, int posY, int width, int height, Consumer<ComponentRaw> func) {
		super(posX, posY, width, height);
		this.func = func;
	}

	@Override
	public void drawComponent(Vec2 mousePos, float partialTicks) {
		func.accept(this);
	}

}
