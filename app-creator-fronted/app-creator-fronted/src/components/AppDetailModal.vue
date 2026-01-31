<template>
  <a-modal v-model:open="visible" title="应用详情" :footer="null" width="500px">
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <div class="info-item">
          <span class="info-label">应用 ID：</span>
          <span>{{ app?.appId ?? '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">应用名称：</span>
          <span>{{ app?.appName ?? '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">创建者：</span>
          <UserInfo :user="app?.userVO" size="small" />
        </div>
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span>{{ formatTime(app?.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">更新时间：</span>
          <span>{{ formatTime(app?.updateTime) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">生成类型：</span>
          <a-tag v-if="app?.codeGenType" color="orange">
            {{ formatCodeGenType(app.codeGenType) }}
          </a-tag>
          <span v-else>未知类型</span>
        </div>
        <div class="info-item">
          <span class="info-label">优先级：</span>
          <span>{{ app?.appPriority ?? 0 }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">部署时间：</span>
          <span>{{ app?.deployedTime ? formatTime(app.deployedTime) : '未部署' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">部署地址：</span>
          <a-button v-if="app?.deployUrl" type="link" @click="openDeployUrl" style="padding: 0; height: auto">
            打开
          </a-button>
          <span v-else>未部署</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="showActions" class="app-actions">
        <a-space>
          <a-button type="primary" @click="handleEdit">
            <template #icon>
              <EditOutlined />
            </template>
            修改
          </a-button>
          <a-popconfirm
            title="确定要删除这个应用吗？"
            @confirm="handleDelete"
            ok-text="确定"
            cancel-text="取消"
          >
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import {formatCodeGenType} from "../utils/codeGenTypes.ts";

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}

const openDeployUrl = () => {
  if (props.app?.deployUrl) {
    window.open(props.app.deployUrl, '_blank')
  }
}
</script>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.app-basic-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.app-actions {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
