<template>
  <q-page class="q-pa-lg bg-grey-2">
    <!-- HEADER -->
    <div class="row items-center q-mb-md">
      <q-icon name="admin_panel_settings" size="32px" color="primary" />
      <div class="text-h5 text-weight-bold q-ml-sm">Administración de Citas</div>
    </div>

    <!-- FILTROS -->
    <q-card class="q-pa-md q-mb-md">
      <!-- TABS DE ESTADO -->
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

      <!-- BUSCADOR -->
      <q-input
        v-model="busqueda"
        dense
        outlined
        debounce="300"
        placeholder="Buscar por correo o teléfono"
        class="q-mt-md"
        clearable
      >
        <template #prepend>
          <q-icon name="search" />
        </template>
      </q-input>
    </q-card>

    <!-- TABLA -->
    <q-card flat bordered>
      <q-table
        flat
        bordered
        separator="cell"
        :rows="citasFiltradas"
        :columns="columns"
        row-key="id"
        :loading="loading"
        rows-per-page-label="Citas por página"
      >
        <!-- FECHA Y HORA -->
        <template v-slot:body-cell-fechaHora="props">
          <div class="text-weight-medium">
            {{ formatearFecha(props.row.fechaHora) }}
          </div>
        </template>

        <!-- SERVICIOS -->
        <template v-slot:body-cell-servicios="props">
          <div class="row q-gutter-xs">
            <q-chip v-for="s in props.row.servicios" :key="s.id" dense outline color="primary">
              {{ s.nombre }}
            </q-chip>
          </div>
        </template>

        <!-- ESTADO -->
        <template v-slot:body-cell-estado="props">
          <div class="column items-center q-gutter-xs">
            <q-chip dense square text-color="white" :color="colorEstado(props.row.estado)">
              {{ props.row.estado }}
            </q-chip>

            <q-select
              dense
              outlined
              emit-value
              map-options
              :options="estados"
              v-model="props.row.estado"
              @update:model-value="cambiarEstado(props.row)"
              style="min-width: 140px"
            />
          </div>
        </template>

        <!-- ACCIONES -->
        <template v-slot:body-cell-acciones>
          <span class="text-grey-5">—</span>
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

/* ===============================
   STATE
================================ */
const citas = ref([])
const loading = ref(false)
const estadoFiltro = ref('TODAS')
const busqueda = ref('')

/* ===============================
   OPTIONS
================================ */
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
    align: 'left',
    sortable: true,
  },
  {
    name: 'correo',
    label: 'Correo',
    field: 'correo',
    align: 'left',
  },
  {
    name: 'telefono',
    label: 'Teléfono',
    field: 'telefono',
    align: 'center',
  },
  {
    name: 'servicios',
    label: 'Servicios',
    align: 'left',
  },
  {
    name: 'fechaHora',
    label: 'Fecha y hora',
    field: 'fechaHora',
    align: 'center',
    sortable: true,
  },
  {
    name: 'estado',
    label: 'Estado',
    align: 'center',
  },
  {
    name: 'acciones',
    label: 'Acciones',
    align: 'center',
  },
]

/* ===============================
   LIFECYCLE
================================ */
onMounted(cargarCitas)

/* ===============================
   METHODS
================================ */
async function cargarCitas() {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8082/api/citas')
    citas.value = res.data
  } finally {
    loading.value = false
  }
}

async function cambiarEstado(cita) {
  await axios.patch(`http://localhost:8082/api/citas/${cita.id}/estado`, null, {
    params: { estado: cita.estado },
  })

  $q.notify({
    type: 'positive',
    message: 'Estado actualizado correctamente',
  })
}

/* ===============================
   COMPUTED
================================ */
const citasFiltradas = computed(() => {
  return citas.value.filter((c) => {
    const okEstado = estadoFiltro.value === 'TODAS' || c.estado === estadoFiltro.value

    const texto = busqueda.value.toLowerCase()
    const okBusqueda =
      !texto || c.correo?.toLowerCase().includes(texto) || c.telefono?.includes(texto)

    return okEstado && okBusqueda
  })
})

/* ===============================
   HELPERS
================================ */
function formatearFecha(fecha) {
  return new Date(fecha).toLocaleString('es-MX', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function colorEstado(estado) {
  return (
    {
      PENDIENTE: 'orange',
      CONFIRMADA: 'blue',
      ATENDIDA: 'green',
      CANCELADA: 'red',
      REAGENDADA: 'purple',
    }[estado] || 'grey'
  )
}
</script>

<style scoped>
.q-page {
  max-width: 1400px;
  margin: auto;
}

.q-table td {
  vertical-align: middle;
}
</style>
