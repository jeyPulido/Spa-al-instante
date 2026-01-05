<template>
  <q-page class="q-pa-lg bg-grey-2">
    <div class="row items-center q-mb-md">
      <q-icon name="admin_panel_settings" size="32px" color="primary" />
      <p class="text-h5 text-weight-bold q-ml-sm">Administración de Citas</p>
    </div>

    <q-card flat bordered>
      <q-table :rows="citas" :columns="columns" row-key="id" :loading="loading" flat>
        <template v-slot:body-cell-fechaHora="props">
          {{ formatearFecha(props.row.fechaHora) }}
        </template>

        <template v-slot:body-cell-acciones="props">
          <q-btn
            color="negative"
            icon="cancel"
            label="Cancelar"
            flat
            size="sm"
            @click="confirmarCancelacion(props.row)"
          />
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'

const $q = useQuasar()
const citas = ref([])
const loading = ref(false)

const columns = [
  {
    name: 'cliente',
    label: 'Cliente',
    field: (row) => `${row.nombreCliente} ${row.apellidosCliente}`,
    sortable: true,
  },
  {
    name: 'correo',
    label: 'Correo',
    field: 'correo',
  },
  {
    name: 'telefono',
    label: 'Teléfono',
    field: 'telefono',
  },
  {
    name: 'servicio',
    label: 'Servicio',
    field: (row) => row.servicio?.nombre,
  },
  {
    name: 'fechaHora',
    label: 'Fecha y Hora',
    field: 'fechaHora',
    sortable: true,
  },
  {
    name: 'acciones',
    label: 'Acciones',
    field: 'acciones',
  },
]

async function cargarCitas() {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8082/api/citas')
    citas.value = res.data
  } catch {
    $q.notify({
      type: 'negative',
      message: 'Error al cargar las citas',
    })
  } finally {
    loading.value = false
  }
}

function confirmarCancelacion(cita) {
  $q.dialog({
    title: 'Cancelar cita',
    message: `¿Cancelar la cita de ${cita.nombreCliente}?`,
    cancel: true,
    persistent: true,
  }).onOk(() => cancelarCita(cita.id))
}

async function cancelarCita(id) {
  try {
    await axios.delete(`http://localhost:8082/api/citas/${id}`)
    $q.notify({
      type: 'positive',
      message: 'Cita cancelada correctamente',
    })
    cargarCitas()
  } catch {
    $q.notify({
      type: 'negative',
      message: 'Error al cancelar la cita',
    })
  }
}

function formatearFecha(fecha) {
  return new Date(fecha).toLocaleString('es-MX', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

onMounted(cargarCitas)
</script>

<style scoped>
.q-page {
  max-width: 1200px;
  margin: auto;
}
</style>
