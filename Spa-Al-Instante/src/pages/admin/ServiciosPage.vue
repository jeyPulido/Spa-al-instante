<template>
  <q-page class="q-pa-md bg-grey-2">
    <q-card class="q-pa-lg q-mb-md">
      <p class="text-h6">➕ Registrar / Editar Servicio</p>

      <q-form @submit.prevent="guardarServicio">
        <q-input
          filled
          v-model="form.nombre"
          label="Nombre del servicio"
          lazy-rules
          :rules="[(v) => !!v || 'Campo requerido']"
        />

        <q-input filled v-model="form.descripcion" label="Descripción" type="textarea" />

        <q-input
          filled
          v-model.number="form.precio"
          label="Precio"
          type="number"
          prefix="$"
          :rules="[(v) => v > 0 || 'Precio inválido']"
        />

        <div class="row q-gutter-sm q-mt-md">
          <q-btn type="submit" color="primary" :label="editando ? 'Actualizar' : 'Guardar'" />
          <q-btn v-if="editando" flat color="negative" label="Cancelar" @click="resetForm" />
        </div>
      </q-form>
    </q-card>

    <!-- LISTA -->
    <q-card class="q-pa-md">
      <p class="text-h6">📋 Servicios Registrados</p>

      <q-list bordered separator>
        <q-item v-for="s in servicios" :key="s.id">
          <q-item-section>
            <q-item-label class="text-weight-bold">{{ s.nombre }}</q-item-label>
            <q-item-label caption>{{ s.descripcion }}</q-item-label>
            <q-item-label caption>$ {{ s.precio }} MXN</q-item-label>
          </q-item-section>

          <q-item-section side>
            <q-btn icon="edit" flat color="primary" @click="editarServicio(s)" />
            <q-btn icon="delete" flat color="negative" @click="eliminarServicio(s.id)" />
          </q-item-section>
        </q-item>
      </q-list>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'

const $q = useQuasar()

const servicios = ref([])
const editando = ref(false)

const form = ref({
  id: null,
  nombre: '',
  descripcion: '',
  precio: null,
})

/* ================= API ================= */
async function cargarServicios() {
  const res = await axios.get('http://localhost:8082/api/servicios')
  servicios.value = res.data
}

async function guardarServicio() {
  try {
    if (editando.value) {
      await axios.put(`http://localhost:8082/api/servicios/${form.value.id}`, form.value)
      $q.notify({ type: 'positive', message: 'Servicio actualizado' })
    } else {
      await axios.post('http://localhost:8082/api/servicios', form.value)
      $q.notify({ type: 'positive', message: 'Servicio registrado' })
    }

    resetForm()
    cargarServicios()
  } catch {
    $q.notify({ type: 'negative', message: 'Error al guardar servicio' })
  }
}

function editarServicio(servicio) {
  form.value = { ...servicio }
  editando.value = true
}

async function eliminarServicio(id) {
  $q.dialog({
    title: 'Confirmar',
    message: '¿Eliminar este servicio?',
    cancel: true,
    persistent: true,
  }).onOk(async () => {
    await axios.delete(`http://localhost:8082/api/servicios/${id}`)
    $q.notify({ type: 'info', message: 'Servicio eliminado' })
    cargarServicios()
  })
}

function resetForm() {
  form.value = {
    id: null,
    nombre: '',
    descripcion: '',
    precio: null,
  }
  editando.value = false
}

onMounted(cargarServicios)
</script>
