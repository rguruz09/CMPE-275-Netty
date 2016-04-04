package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class CommandsUtils {

    public static Work.WorkMessage getWorkFromCommand(Pipe.CommandMessage msg, ServerState state){

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(state.getElectionMonitor().getLeaderStatus().getCurLeader());
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.Command.Builder cb = Work.Command.newBuilder();
        if(msg.hasQuery())
            cb.setQuery(msg.getQuery());
        if(msg.hasResponse())
            cb.setResponse(msg.getResponse());

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setCommand(cb);
        wb.setSecret(123);

        return wb.build();
    }
}
