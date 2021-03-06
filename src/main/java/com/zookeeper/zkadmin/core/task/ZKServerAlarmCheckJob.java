package com.zookeeper.zkadmin.core.task;

import com.zookeeper.zkadmin.core.GlobalInstance;
import com.zookeeper.zkadmin.core.InitSystemManager;
import com.zookeeper.zkadmin.core.ThreadPoolManager;
import com.zookeeper.zkadmin.dao.ZooKeeperClusterDAO;
import com.zookeeper.zkadmin.dao.ZooKeeperServerDAO;
import com.zookeeper.zkadmin.entities.model.AlarmSetting;
import com.zookeeper.zkadmin.entities.model.ZooKeeperCluster;
import com.zookeeper.zkadmin.entities.model.ZooKeeperServer;
import com.zookeeper.zkadmin.entities.model.metrics.Metrics;
import com.zookeeper.zkadmin.entities.model.metrics.impl.MntrMetrics;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.BlockingDeque;

/**
 * @author lamber-ken
 * @date 2017-01-05 19:49
 * @since JDK1.7
 */
public class ZKServerAlarmCheckJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("SYSTEM_LOGGER");
    private static final GlobalInstance gInstance = GlobalInstance.getInstance();
    private static final BlockingDeque<Metrics> queue = gInstance.getMQueue();


    @Override
    public void run() {
        while (true) {

            try {

                ApplicationContext ac = InitSystemManager.getApplicationContext();
                ZooKeeperClusterDAO clusterDAO = ac.getBean(ZooKeeperClusterDAO.class);
                ZooKeeperServerDAO serverDAO =  ac.getBean(ZooKeeperServerDAO.class);

                Metrics metrics = queue.take();

                if (metrics != null && metrics instanceof MntrMetrics) {
                    MntrMetrics mm = (MntrMetrics) metrics;

                    AlarmSetting alarmSetting = gInstance.getAlarmSetting(mm.getClusterID());
                    ZooKeeperCluster zooCluster = clusterDAO.getZooKeeperCluster(mm.getClusterID());
                    ZooKeeperServer zooServer = serverDAO.getZooKeeperServer(mm.getClusterID(), mm.getServerID());

                    if (null != zooCluster && zooServer != null && null != alarmSetting) {

                        long diff = mm.getNumAliveConnections() - alarmSetting.getMaxConnectionPerIp();
                        if (diff > 0) {


                            StringBuffer sb = new StringBuffer();
                            sb.append("时间: " + DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS"));
                            sb.append("\n");
                            sb.append("内容: \n");
                            sb.append("\t");
                            sb.append(String.format("%s:%s 上的连接数达到了: %s, 超过报警阀值: %s", zooServer.getHost(), zooServer.getPort(), mm.getNumAliveConnections(), alarmSetting.getMaxConnectionPerIp()));

                            ThreadPoolManager.addZKServerMessageExecutor(new ZKMessageSender(
                                    zooCluster.getClusterName() +" ZK集群"+ " 预警信息",
                                    sb.toString(),
                                    alarmSetting.getJobNumList(),
                                    alarmSetting.getRouteChannelIDs()

                            ));
                        }
                    }
                }


            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {


        String message = String.format("ZK Server %s:%s 上的连接数达到了: %s, 超过设置的报警阀值: %s", 12, 123, 123, 123);
        System.out.println(message);

    }


}