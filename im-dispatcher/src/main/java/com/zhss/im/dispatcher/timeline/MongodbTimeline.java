package com.zhss.im.dispatcher.timeline;

import java.util.List;

/**
 * 基于MongoDB实现的Timeline模型
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 18:38
 */
public class MongodbTimeline implements Timeline {

    @Override
    public List<TimelineMessage> fetchMessage(FetchRequest request) {
        // TODO
        return null;
    }

    @Override
    public void saveMessage(TimelineMessage timelineMessage) {
        // TODo
    }
}
