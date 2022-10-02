package me.freesign.utils;

import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    //Renders A box
    public static void renderBox(BlockPos pos1, BlockPos pos2, int red, int green, int blue){
        GL11.glColor4ub((byte)red,(byte)green,(byte)blue,(byte)255);
        GL11.glLineWidth(3);

        //IDK why you have to add such specific values to Y but that's the way it is
        float x = pos1.getX();
        float y = pos1.getY()+0.09375f;
        float z = pos1.getZ();

        float x1 = pos2.getX()+1f;
        float y1 = pos2.getY()+1.09375f;
        float z1 = pos2.getZ()+1f;

        //Draw all the lines of thew Bounding box
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x,y,z);
        GL11.glVertex3f(x1,y,z);
        GL11.glVertex3f(x,y,z);
        GL11.glVertex3f(x,y1,z);
        GL11.glVertex3f(x1,y,z);
        GL11.glVertex3f(x1,y,z1);
        GL11.glVertex3f(x1,y,z);
        GL11.glVertex3f(x1,y1,z);
        GL11.glVertex3f(x1,y,z1);
        GL11.glVertex3f(x,y,z1);
        GL11.glVertex3f(x1,y,z1);
        GL11.glVertex3f(x1,y1,z1);
        GL11.glVertex3f(x,y,z1);
        GL11.glVertex3f(x,y,z);
        GL11.glVertex3f(x,y,z1);
        GL11.glVertex3f(x,y1,z1);
        GL11.glVertex3f(x,y1,z);
        GL11.glVertex3f(x1,y1,z);
        GL11.glVertex3f(x1,y1,z);
        GL11.glVertex3f(x1,y1,z1);
        GL11.glVertex3f(x1,y1,z1);
        GL11.glVertex3f(x,y1,z1);
        GL11.glVertex3f(x,y1,z1);
        GL11.glVertex3f(x,y1,z);
        GL11.glEnd();
    }

    //render the outline of a block
    public static void renderBlockOutline(BlockPos pos,int red,int green,int blue){
        renderBox(pos,pos,red,green,blue);
    }
}
