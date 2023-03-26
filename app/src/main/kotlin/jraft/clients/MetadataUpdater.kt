package jraft.clients

interface MetadataUpdater {
    /**
     * Returns true if an update to the cluster metadata info is due.
     *
     * 클러스터 메타정보 업데이트가 예정 되어 있는 경우.
     */
    fun isUpdateDue(): Boolean
}
