// Copyright (c) 2017, Baidu.com, Inc. All Rights Reserved

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#ifndef BDG_PALO_BE_SRC_TASK_WORKER_POOL_H
#define BDG_PALO_BE_SRC_TASK_WORKER_POOL_H

#include <atomic>
#include <deque>
#include <utility>
#include <vector>
#include "agent/pusher.h"
#include "agent/status.h"
#include "agent/utils.h"
#include "gen_cpp/AgentService_types.h"
#include "gen_cpp/HeartbeatService_types.h"
#include "olap/command_executor.h"
#include "olap/olap_define.h"
#include "olap/utils.h"

namespace palo {

const uint32_t DOWNLOAD_FILE_MAX_RETRY = 3;
const uint32_t TASK_FINISH_MAX_RETRY = 3;
const uint32_t PUSH_MAX_RETRY = 1;
const uint32_t REPORT_TASK_WORKER_COUNT = 1;
const uint32_t REPORT_DISK_STATE_WORKER_COUNT = 1;
const uint32_t REPORT_OLAP_TABLE_WORKER_COUNT = 1;
const uint32_t LIST_REMOTE_FILE_TIMEOUT = 15;
const std::string HTTP_REQUEST_PREFIX = "/api/_tablet/_download?file=";

class TaskWorkerPool {
public:
    enum TaskWorkerType {
        CREATE_TABLE,
        DROP_TABLE,
        PUSH,
        DELETE,
        ALTER_TABLE,
        QUERY_SPLIT_KEY,
        CLONE,
        STORAGE_MEDIUM_MIGRATE,
        CANCEL_DELETE_DATA,
        CHECK_CONSISTENCY,
        REPORT_TASK,
        REPORT_DISK_STATE,
        REPORT_OLAP_TABLE,
        UPLOAD,
        RESTORE,
        MAKE_SNAPSHOT,
        RELEASE_SNAPSHOT
    };

    typedef void* (*CALLBACK_FUNCTION)(void*);

    TaskWorkerPool(
            const TaskWorkerType task_worker_type,
            const TMasterInfo& master_info);
    virtual ~TaskWorkerPool();

    // Start the task worker callback thread
    virtual void start();

    // Submit task to task pool
    //
    // Input parameters:
    // * task: the task need callback thread to do
    virtual void submit_task(const TAgentTaskRequest& task);

private:
    bool _record_task_info(
            const TTaskType::type task_type, int64_t signature, const std::string& user);
    void _remove_task_info(
            const TTaskType::type task_type, int64_t signature, const std::string& user);
    void _spawn_callback_worker_thread(CALLBACK_FUNCTION callback_func);
    void _finish_task(const TFinishTaskRequest& finish_task_request);
    uint32_t _get_next_task_index(int32_t thread_count, std::deque<TAgentTaskRequest>& tasks,
            TPriority::type priority);

    static void* _create_table_worker_thread_callback(void* arg_this);
    static void* _drop_table_worker_thread_callback(void* arg_this);
    static void* _push_worker_thread_callback(void* arg_this);
    static void* _alter_table_worker_thread_callback(void* arg_this);
    static void* _clone_worker_thread_callback(void* arg_this);
    static void* _storage_medium_migrate_worker_thread_callback(void* arg_this);
    static void* _cancel_delete_data_worker_thread_callback(void* arg_this);
    static void* _check_consistency_worker_thread_callback(void* arg_this);
    static void* _report_task_worker_thread_callback(void* arg_this);
    static void* _report_disk_state_worker_thread_callback(void* arg_this);
    static void* _report_olap_table_worker_thread_callback(void* arg_this);
    static void* _upload_worker_thread_callback(void* arg_this);
    static void* _restore_worker_thread_callback(void* arg_this);
    static void* _make_snapshot_thread_callback(void* arg_this);
    static void* _release_snapshot_thread_callback(void* arg_this);

    AgentStatus _clone_copy(
            const TCloneReq& clone_req,
            int64_t signature,
            const std::string& local_data_path,
            TBackend* src_host,
            std::string* src_file_path,
            std::vector<std::string>* error_msgs);

    void _alter_table(
            const TAlterTabletReq& create_rollup_request,
            int64_t signature,
            const TTaskType::type task_type,
            TFinishTaskRequest* finish_task_request);

    AlterTableStatus _show_alter_table_status(
            const TTabletId tablet_id,
            const TSchemaHash schema_hash);

    AgentStatus _drop_table(const TDropTabletReq drop_tablet_req);

    AgentStatus _get_tablet_info(
            const TTabletId tablet_id,
            const TSchemaHash schema_hash,
            int64_t signature,
            TTabletInfo* tablet_info);

    const TMasterInfo& _master_info;
    TBackend _backend;
    AgentUtils* _agent_utils;
    MasterServerClient* _master_client;
    CommandExecutor* _command_executor;
#ifdef BE_TEST
    AgentServerClient* _agent_client;
    FileDownloader* _file_downloader_ptr;
    Pusher * _pusher;
#endif

    std::deque<TAgentTaskRequest> _tasks;
    MutexLock _worker_thread_lock;
    Condition _worker_thread_condition_lock;
    uint32_t _worker_count;
    TaskWorkerType _task_worker_type;
    CALLBACK_FUNCTION _callback_function;
    static std::atomic_ulong _s_report_version;
    static std::map<TTaskType::type, std::set<int64_t>> _s_task_signatures;
    static std::map<TTaskType::type, std::map<std::string, uint32_t>> _s_running_task_user_count;
    static std::map<TTaskType::type, std::map<std::string, uint32_t>> _s_total_task_user_count;
    static std::map<TTaskType::type, uint32_t> _s_total_task_count;
    static MutexLock _s_task_signatures_lock;
    static MutexLock _s_running_task_user_count_lock;
    static FrontendServiceClientCache _master_service_client_cache;

    DISALLOW_COPY_AND_ASSIGN(TaskWorkerPool);
};  // class TaskWorkerPool
}  // namespace palo
#endif  // BDG_PALO_BE_SRC_TASK_WORKER_POOL_H
