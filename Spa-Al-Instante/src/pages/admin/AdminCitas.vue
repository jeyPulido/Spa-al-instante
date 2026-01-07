<template>
  <q-page class="q-pa-lg bg-grey-2">
    <!-- HEADER -->
    <q-card class="q-pa-md q-mb-lg bg-primary text-white">
      <div class="row items-center justify-between">
        <div class="row items-center">
          <q-icon name="admin_panel_settings" size="36px" />
          <div class="text-h5 text-weight-bold q-ml-sm">Administración de Citas</div>
        </div>

        <q-btn flat icon="refresh" label="Recargar" @click="cargarCitas" />
      </div>
    </q-card>

    <!-- KPIs -->
    <div class="row q-col-gutter-md q-mb-lg">
      <q-card class="col bg-white q-pa-md" v-for="kpi in kpis" :key="kpi.label">
        <div class="row items-center justify-between">
          <div>
            <div class="text-subtitle2 text-grey-7">{{ kpi.label }}</div>
            <div class="text-h5 text-weight-bold">{{ kpi.value }}</div>
          </div>
          <q-icon :name="kpi.icon" size="32px" :color="kpi.color" />
        </div>
      </q-card>
    </div>

    <!-- FILTROS -->
    <q-card class="q-pa-md q-mb-md">
      <q-tabs
        v-model="estadoFiltro"
        dense
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="TODAS" label="Todas" />
        <q-tab name="PENDIENTE" label="Pendientes" />
        <q-tab name="CONFIRMADA" label="Confirmadas" />
        <q-tab name="ATENDIDA" label="Atendidas" />
        <q-tab name="CANCELADA" label="Canceladas" />
        <q-tab name="REAGENDADA" label="Reagendadas" />
      </q-tabs>

      <div class="row q-col-gutter-md q-mt-md">
        <q-input
          v-model="busqueda"
          dense
          outlined
          debounce="300"
          placeholder="Buscar por correo o teléfono"
          class="col"
          clearable
        >
          <template #prepend>
            <q-icon name="search" />
          </template>
        </q-input>

        <q-btn outline icon="filter_alt_off" label="Limpiar filtros" @click="limpiarFiltros" />
      </div>
    </q-card>

    <!-- TABLA -->
    <q-card flat bordered>
      <q-table
        flat
        bordered
        separator="horizontal"
        :rows="citasFiltradas"
        :columns="columns"
        row-key="id"
        :loading="loading"
      >
        <!-- FECHA -->
        <template v-slot:body-cell-fechaHora="props">
          <div class="text-weight-medium">
            {{ formatearFecha(props.row.fechaHora) }}
          </div>
        </template>

        <!-- SERVICIOS -->
        <template v-slot:body-cell-servicios="props">
          <q-chip
            v-for="s in props.row.servicios"
            :key="s.id"
            dense
            outline
            color="primary"
            class="q-mr-xs"
          >
            {{ s.nombre }}
          </q-chip>
        </template>

        <!-- ESTADO -->
        <template v-slot:body-cell-estado="props">
          <q-select
            dense
            outlined
            emit-value
            map-options
            :options="estados"
            v-model="props.row.estado"
            @update:model-value="cambiarEstado(props.row)"
            style="min-width: 140px"
          >
            <template #prepend>
              <q-icon name="circle" :color="colorEstado(props.row.estado)" />
            </template>
          </q-select>
        </template>

        <!-- ACCIONES -->
        <template v-slot:body-cell-acciones="props">
          <q-btn flat dense round icon="cancel" color="red" @click="cancelarCita(props.row)" />
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'

const $q = useQuasar()

const citas = ref([])
const loading = ref(false)
const estadoFiltro = ref('TODAS')
const busqueda = ref('')

const estados = [
  { label: 'Pendiente', value: 'PENDIENTE' },
  { label: 'Confirmada', value: 'CONFIRMADA' },
  { label: 'Atendida', value: 'ATENDIDA' },
  { label: 'Cancelada', value: 'CANCELADA' },
  { label: 'Reagendada', value: 'REAGENDADA' },
]

const columns = [
  {
    name: 'cliente',
    label: 'Cliente',
    field: (row) => `${row.nombreCliente} ${row.apellidosCliente}`,
  },
  { name: 'correo', label: 'Correo', field: 'correo' },
  { name: 'telefono', label: 'Teléfono', field: 'telefono', align: 'center' },
  { name: 'servicios', label: 'Servicios' },
  { name: 'fechaHora', label: 'Fecha y hora', field: 'fechaHora', sortable: true },
  { name: 'estado', label: 'Estado', align: 'center' },
  { name: 'acciones', label: 'Acciones', align: 'center' },
]

onMounted(cargarCitas)

async function cargarCitas() {
  loading.value = true
  const res = await axios.get('http://localhost:8082/api/citas')
  citas.value = res.data
  loading.value = false
}

function limpiarFiltros() {
  estadoFiltro.value = 'TODAS'
  busqueda.value = ''
}

async function cambiarEstado(cita) {
  await axios.patch(`http://localhost:8082/api/citas/${cita.id}/estado`, null, {
    params: { estado: cita.estado },
  })
  $q.notify({ type: 'positive', message: 'Estado actualizado' })
}

async function cancelarCita(cita) {
  $q.dialog({
    title: 'Cancelar cita',
    message: '¿Seguro que deseas cancelar esta cita?',
    cancel: true,
    persistent: true,
  }).onOk(async () => {
    cita.estado = 'CANCELADA'
    await cambiarEstado(cita)
  })
}

const citasFiltradas = computed(() =>
  citas.value.filter((c) => {
    const okEstado = estadoFiltro.value === 'TODAS' || c.estado === estadoFiltro.value
    const t = busqueda.value.toLowerCase()
    const okBusqueda = !t || c.correo?.toLowerCase().includes(t) || c.telefono?.includes(t)
    return okEstado && okBusqueda
  }),
)

const kpis = computed(() => [
  { label: 'Total', value: citas.value.length, icon: 'event', color: 'primary' },
  {
    label: 'Pendientes',
    value: citas.value.filter((c) => c.estado === 'PENDIENTE').length,
    icon: 'schedule',
    color: 'orange',
  },
  {
    label: 'Confirmadas',
    value: citas.value.filter((c) => c.estado === 'CONFIRMADA').length,
    icon: 'check_circle',
    color: 'blue',
  },
  {
    label: 'Canceladas',
    value: citas.value.filter((c) => c.estado === 'CANCELADA').length,
    icon: 'cancel',
    color: 'red',
  },
])

function formatearFecha(fecha) {
  return new Date(fecha).toLocaleString('es-MX', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function colorEstado(e) {
  return {
    PENDIENTE: 'orange',
    CONFIRMADA: 'blue',
    ATENDIDA: 'green',
    CANCELADA: 'red',
    REAGENDADA: 'purple',
  }[e]
}
</script>
