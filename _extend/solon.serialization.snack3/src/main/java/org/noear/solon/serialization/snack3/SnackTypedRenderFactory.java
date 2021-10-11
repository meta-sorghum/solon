package org.noear.solon.serialization.snack3;

import org.noear.snack.core.Constants;
import org.noear.snack.core.NodeEncoder;
import org.noear.solon.core.handle.Render;
import org.noear.solon.core.handle.RenderFactory;

/**
 * @author noear
 * @since 1.5
 */
public class SnackTypedRenderFactory implements RenderFactory {
    public static final SnackTypedRenderFactory instance = new SnackTypedRenderFactory();


    private final Constants config;

    private SnackTypedRenderFactory(){
        config = Constants.serialize();
    }

    /**
     * 添加编码器
     * */
    public <T> void addEncoder(Class<T> clz, NodeEncoder<T> encoder) {
        config.addEncoder(clz, encoder);
    }

    @Override
    public Render create() {
        return new SnackRender(true, config);
    }
}
