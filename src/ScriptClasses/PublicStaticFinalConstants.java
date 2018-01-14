package ScriptClasses;

import GUI.UserSelectedResults;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.Script;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class PublicStaticFinalConstants {
    public static final String SCRIPT_NAME = "Splash_Alcher";
    public static final int RS_GAME_TICK_MS = 603;
    public static final double BETWEEN_ALCH_STDDEV_MS = 20;
    public static final double BETWEEN_ALCH_MEAN_MS = 215;

    //used to obtain widget destinations
    public static final int NORMAL_SPELLBOOK_ROOT_ID = 218;
    public static final int HIGH_ALCH_SPRITE_ID = 41;
    public static final int CURSE_SPRITE_ID = 24;
    public static final int VULERABILITY_SPRITE_ID = 56;
    public static final int ENFEEBLE_SPRITE_ID = 57;
    public static final int STUN_SPRITE_ID = 58;

    //used to hover over the next spell
    public static WidgetDestination highAlchWidgetDestination = null;
    public static WidgetDestination splashSpellWidgetDestination = null;

    //leave these here for now in case I need them again
    public static final Point CURSE_UPPER_LEFT_BOUND = new Point(658, 248);
    public static final Point CURSE_LOWER_RIGHT_BOUND = new Point(677, 265);
    public static final Point VULNERABILITY_UPPER_LEFT_BOUND = new Point(586, 391);
    public static final Point VULNERABILITY_LOWER_RIGHT_BOUND = new Point(605, 409);
    public static final Point ENFEEBLE_UPPER_LEFT_BOUND = new Point(682, 391);
    public static final Point ENFEEBLE_LOWER_RIGHT_BOUND = new Point(700, 409);
    public static final Point STUN_UPPER_LEFT_BOUND = new Point(613, 417);
    public static final Point STUN_LOWER_RIGHT_BOUND = new Point(626, 431);

    //for antiban
    public static final Point EARTH_BLAST_UPPER_LEFT_BOUND = new Point(686,322);
    public static final Point EARTH_BLAST_LOWER_RIGHT_BOUND = new Point(700,336);
    public static final Point ALCH_NOTHING_UPPER_LEFT_BOUND = new Point(721,319);
    public static final Point ALCH_NOTHING_LOWER_RIGHT_BOUND = new Point(725,337);

    //queried from user
    public static int targetNPC;
    public static int targetItem;
    public static Spells.NormalSpells splashingSpell;
    public static int totalCastableSpells;

    public static Script hostScriptReference;

    private PublicStaticFinalConstants(){} //meant to be a constant provider, no constructor

    public static boolean canCast(){
        return totalCastableSpells-- > 0;
    }

    public static long randomNormalDist(double mean, double stddev){
        return (long) Math.abs(new Random().nextGaussian() * stddev + mean); //in case we get a negative number
    }

    public static boolean hoverOverSplashSpell(){
        if(splashSpellWidgetDestination != null){
            return hostScriptReference.getMouse().move(splashSpellWidgetDestination);
        }
        else{
            throwIllegalStateException("splashSpellWidgetDestination is null");
            return false;
        }
    }

    //meant to be called once at start of script
    private static void setSplashSpellWidgetDestination(){
        if(splashSpellWidgetDestination == null){
            if(hostScriptReference != null){

                hostScriptReference.log("setting " + splashingSpell + " as splashing spell");
                switch (splashingSpell){
                    case CURSE:
                        splashSpellWidgetDestination = getWidgetDestinationForSpell(CURSE_SPRITE_ID);
                        break;

                    case VULNERABILITY:
                        splashSpellWidgetDestination = getWidgetDestinationForSpell(VULERABILITY_SPRITE_ID);
                        break;

                    case ENFEEBLE:
                        splashSpellWidgetDestination = getWidgetDestinationForSpell(ENFEEBLE_SPRITE_ID);
                        break;

                    case STUN:
                        splashSpellWidgetDestination = getWidgetDestinationForSpell(STUN_SPRITE_ID);
                        break;

                    default:
                        throwIllegalStateException("hit default case in splashing spell");
                }
            }
            else{
                throwIllegalStateException("hostScriptReference is null");
            }
        }
    }

    //meant to be called once at start of script to get mouse hover destinations after stunning or alching
    private static void setHighAlchWidgetDestination(){
        hostScriptReference.getMagic().open();
        if(splashSpellWidgetDestination == null){
            if(hostScriptReference != null){
                List<RS2Widget> widgetList = hostScriptReference.getWidgets().containingSprite(NORMAL_SPELLBOOK_ROOT_ID, HIGH_ALCH_SPRITE_ID);
                highAlchWidgetDestination = getWidgetDestinationForSpell(HIGH_ALCH_SPRITE_ID);
            }
            else{
                throwIllegalStateException("hostScriptReference is null");
            }
        }
    }

    //meant to be called once at start of script to get mouse hover destinations after stunning or alching
    private static WidgetDestination getWidgetDestinationForSpell(int spellSpriteID){
        hostScriptReference.getMagic().open();
        int widgetErrorCounter = 0;
        List<RS2Widget> widgetList = hostScriptReference.getWidgets().containingSprite(NORMAL_SPELLBOOK_ROOT_ID, spellSpriteID);
        //sometimes the above method call returns an empty array, in that case attempt to re-query the widget up to 5 times before stopping
        while(widgetList.size() == 0 && widgetErrorCounter <= 5){
            widgetErrorCounter++;
            widgetList = hostScriptReference.getWidgets().containingSprite(NORMAL_SPELLBOOK_ROOT_ID, spellSpriteID);
        }

        hostScriptReference.log("got a widget after " + (widgetErrorCounter + 1) + " tries.");
        if(widgetList.size() >= 1){
            RS2Widget spellWidget = widgetList.get(0);
            return new WidgetDestination(hostScriptReference.bot, spellWidget);
        }

        else{
            throwIllegalStateException("failed 5 times to query widget, got " +widgetList.size()+ " RS2Widgets when querying spellSpriteID: " + spellSpriteID + "\ntry restarting the script as this may be a client error");
            return null;
        }
    }

    //for errors
    public static void throwIllegalStateException(String msg){
        hostScriptReference.stop(false);
        throw new IllegalStateException(msg);
    }

    public static void setUserSettings(UserSelectedResults results){
        PublicStaticFinalConstants.targetNPC = results.getNpcID();
        PublicStaticFinalConstants.targetItem = results.getItemID();
        PublicStaticFinalConstants.splashingSpell = results.getSplashingSpell();

        setSplashSpellWidgetDestination();
        setHighAlchWidgetDestination();
    }

    public static void setHostScriptReference(Script ref){
        hostScriptReference = ref;
    }

    public static void setTotalCastableSpells(int num){
        totalCastableSpells = num;
    }
}
