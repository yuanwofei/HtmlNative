package com.mozz.htmlnative.script.lua;

import android.view.View;
import android.view.ViewGroup;

import com.mozz.htmlnative.HNSandBoxContext;
import com.mozz.htmlnative.attrshandler.AttrHandler;
import com.mozz.htmlnative.attrshandler.AttrsHelper;
import com.mozz.htmlnative.attrshandler.LayoutAttrHandler;
import com.mozz.htmlnative.css.Styles;
import com.mozz.htmlnative.dom.DomElement;
import com.mozz.htmlnative.exception.AttrApplyException;
import com.mozz.htmlnative.parser.CssParser;
import com.mozz.htmlnative.utils.MainHandlerUtils;
import com.mozz.htmlnative.view.LayoutParamsLazyCreator;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yang Tao, 17/3/23.
 */

public class LView extends LuaTable {

    private View mView;
    private LayoutParamsLazyCreator mLayoutParamsCreator;
    boolean mAdded = false;

    private static StringBuilder sParserBuffer = new StringBuilder();

    public LView(final View v, final LayoutParamsLazyCreator layoutParamsCreator, final
    HNSandBoxContext context) {
        mView = v;
        mLayoutParamsCreator = layoutParamsCreator;
        set("toString", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LView.valueOf(v.toString());
            }
        });

        set("setStyle", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                String style = arg.tojstring();


                final Map<String, Object> styleMaps = new HashMap<>();
                CssParser.parseInlineStyle(style, sParserBuffer, styleMaps);

                final AttrHandler viewAttrHandler = AttrsHelper.getAttrHandler(v);
                final AttrHandler extraAttrHandler = AttrsHelper.getExtraAttrHandler(v);
                final LayoutAttrHandler parentAttr = AttrsHelper.getParentAttrHandler(v);
                MainHandlerUtils.instance().post(new Runnable() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, Object> entry : styleMaps.entrySet()) {
                            ViewGroup parent = null;
                            if (v.getParent() instanceof ViewGroup) {
                                parent = (ViewGroup) v.getParent();
                            }
                            try {
                                Styles.applyStyle(v.getContext(), context, v, null, null, parent,
                                        viewAttrHandler, extraAttrHandler, parentAttr, entry
                                                .getKey(), entry.getValue(), false, null);
                            } catch (AttrApplyException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return NIL;
            }

        });

        set("getId", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                Object obj = v.getTag();
                if (obj != null && obj instanceof DomElement) {
                    return LuaString.valueOf(((DomElement) obj).getId());
                }
                return LuaString.valueOf("");
            }
        });

        set("appendChild", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                if (arg instanceof LView) {
                    LView child = (LView) arg;
                    if (v instanceof ViewGroup && !child.mAdded) {
                        ((ViewGroup) v).addView(child.mView, LayoutParamsLazyCreator
                                .createLayoutParams(v, child.mLayoutParamsCreator));
                    }
                }
                return LuaValue.NIL;
            }
        });
    }

    public LView(final View v, final HNSandBoxContext context) {
        this(v, null, context);
    }


    @Override
    public int type() {
        return TUSERDATA;
    }

    @Override
    public String typename() {
        return TYPE_NAMES[3];
    }

}
