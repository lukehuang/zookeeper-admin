package com.zookeeper.zkadmin.dao.impl.memory;

import com.zookeeper.zkadmin.dao.ZooKeeperClusterDAO;
import com.zookeeper.zkadmin.entities.model.ZooKeeperCluster;
import com.zookeeper.zkadmin.exception.DaoException;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ZooKeeperClusterDAOImpl implements ZooKeeperClusterDAO {

    Map<String, ZooKeeperCluster> clusters_map = new ConcurrentHashMap<>();


    @Override
    public void addZooKeeperCluster(ZooKeeperCluster zooKeeperCluster) throws DaoException {
        clusters_map.put(zooKeeperCluster.getClusterID(), zooKeeperCluster);
    }

    @Override
    public ZooKeeperCluster getZooKeeperCluster(String clusterId) throws DaoException {
        return clusters_map.get(clusterId);
    }

    @Override
    public List<ZooKeeperCluster> getAllZooKeeperClusters() throws DaoException {
        return new LinkedList<>(clusters_map.values());
    }

    @Override
    public boolean removeZooKeeperCluster(String clusterID) throws DaoException {
        clusters_map.remove(clusterID);
        return true;
    }

    @Override
    public void removeAllZooKeeperCluster() throws DaoException {
        clusters_map.clear();
    }

    @Override
    public boolean updateZooKeeperCluster(ZooKeeperCluster zooKeeperCluster) throws DaoException {
        clusters_map.put(zooKeeperCluster.getClusterID(), zooKeeperCluster);
        return false;
    }


}
