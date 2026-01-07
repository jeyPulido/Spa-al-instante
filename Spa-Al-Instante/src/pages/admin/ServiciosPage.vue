<template>
  <q-page class="q-pa-lg bg-grey-2">
    <!-- HEADER -->
    <q-card class="q-pa-md q-mb-lg bg-primary text-white">
      <div class="row items-center justify-between">
        <div class="row items-center">
          <q-icon name="spa" size="36px" />
          <div class="text-h5 text-weight-bold q-ml-sm">Administración de Servicios</div>
        </div>
      </div>
    </q-card>

    <!-- FORMULARIO -->
    <q-card class="q-pa-lg q-mb-lg">
      <div class="row items-center q-mb-md">
        <q-icon :name="editando ? 'edit' : 'add_circle'" size="28px" color="primary" />
        <div class="text-h6 text-weight-bold q-ml-sm">
          {{ editando ? 'Editar servicio' : 'Registrar nuevo servicio' }}
        </div>
      </div>

      <q-form @submit.prevent="guardarServicio" class="q-gutter-md">
        <q-input
          filled
          v-model="form.nombre"
          label="Nombre del servicio"
          lazy-rules
          :rules="[(v) => !!v || 'Campo requerido']"
        >
          <template #prepend>
            <q-icon name="label" />
          </template>
        </q-input>

        <q-input filled v-model="form.descripcion" label="Descripción" type="textarea" autogrow>
          <template #prepend>
            <q-icon name="description" />
          </template>
        </q-input>

        <q-input
          filled
          v-model.number="form.precio"
          label="Precio"
          type="number"
          prefix="$"
          :rules="[(v) => v > 0 || 'Precio inválido']"
        >
          <template #prepend>
            <q-icon name="payments" />
          </template>
        </q-input>

        <div class="row q-gutter-sm q-mt-md">
          <q-btn
            type="submit"
            color="primary"
            icon="save"
            :label="editando ? 'Actualizar servicio' : 'Guardar servicio'"
          />

          <q-btn
            v-if="editando"
            flat
            color="negative"
            icon="close"
            label="Cancelar edición"
            @click="resetForm"
          />
        </div>
      </q-form>
    </q-card>

    <!-- LISTA DE SERVICIOS -->
    <q-card class="q-pa-md">
      <div class="row items-center q-mb-md">
        <q-icon name="list_alt" size="28px" color="primary" />
        <div class="text-h6 text-weight-bold q-ml-sm">Servicios registrados</div>
      </div>

      <q-list bordered separator v-if="servicios.length">
        <q-item v-for="s in servicios" :key="s.id" clickable>
          <q-item-section>
            <q-item-label class="text-weight-bold text-primary">
              {{ s.nombre }}
            </q-item-label>

            <q-item-label caption class="q-mt-xs">
              {{ s.descripcion || 'Sin descripción' }}
            </q-item-label>

            <q-item-label class="q-mt-xs text-weight-medium">
              💰 {{ formatoPrecio(s.precio) }}
            </q-item-label>
          </q-item-section>

          <q-item-section side>
            <div class="row q-gutter-xs">
              <q-btn round dense icon="edit" color="primary" @click="editarServicio(s)" />
              <q-btn round dense icon="delete" color="negative" @click="eliminarServicio(s.id)" />
            </div>
          </q-item-section>
        </q-item>
      </q-list>

      <div v-else class="text-center text-grey q-pa-md">No hay servicios registrados</div>
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
      $q.notify({ type: 'positive', message: 'Servicio actualizado correctamente' })
    } else {
      await axios.post('http://localhost:8082/api/servicios', form.value)
      $q.notify({ type: 'positive', message: 'Servicio registrado correctamente' })
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

  $q.notify({
    type: 'info',
    message: 'Editando servicio',
  })
}

async function eliminarServicio(id) {
  $q.dialog({
    title: 'Eliminar servicio',
    message: '¿Seguro que deseas eliminar este servicio?',
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

/* ================= HELPERS ================= */
function formatoPrecio(precio) {
  return `$${Number(precio).toLocaleString('es-MX')} MXN`
}

onMounted(cargarServicios)
</script>
