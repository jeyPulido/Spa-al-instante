<template>
  <q-page class="q-pa-md bg-grey-2">
    <q-inner-loading :showing="loading">
      <q-spinner-gears size="60px" color="primary" />
      <div class="q-mt-md text-primary text-weight-bold">Cargando información del dashboard...</div>
    </q-inner-loading>

    <q-banner v-if="alerta.visible" :class="`bg-${alerta.color} text-white q-mb-md`" rounded dense>
      <q-icon :name="alerta.icono" class="q-mr-sm" />
      {{ alerta.mensaje }}
      <template #action>
        <q-btn flat dense icon="close" @click="alerta.visible = false" />
      </template>
    </q-banner>

    <div class="row items-center q-mb-md">
      <q-icon name="dashboard" size="28px" color="primary" />
      <p class="text-h6 text-weight-bold q-ml-sm">Dashboard – Spa al Instante</p>
    </div>

    <q-card class="q-mb-md">
      <q-card-section class="row q-col-gutter-sm">
        <div class="col-12 col-sm-4">
          <q-input filled dense v-model="filtroInicio" label="Fecha inicio" type="date" />
        </div>

        <div class="col-12 col-sm-4">
          <q-input filled dense v-model="filtroFin" label="Fecha fin" type="date" />
        </div>

        <div class="col-12 col-sm-4 row q-gutter-sm">
          <q-btn
            class="col"
            color="primary"
            icon="filter_alt"
            label="Aplicar"
            @click="aplicarFiltros"
          />
          <q-btn
            class="col"
            flat
            color="grey-7"
            icon="clear"
            label="Limpiar"
            @click="limpiarFiltros"
          />
        </div>
      </q-card-section>
    </q-card>

    <div class="row q-col-gutter-sm q-mb-md">
      <div class="col-12 col-sm-6 col-md-4 col-lg-2">
        <MetricCard title="💰 Ingresos" :value="`$ ${totalIngresos}`" />
      </div>

      <div class="col-12 col-sm-6 col-md-4 col-lg-2">
        <MetricCard title="📊 Atendidas" :value="totalCitas" />
      </div>

      <div class="col-12 col-sm-6 col-md-4 col-lg-2">
        <MetricCard title="🔁 Recurrentes" :value="clientesRecurrentes" />
      </div>

      <div class="col-12 col-sm-6 col-md-4 col-lg-3">
        <MetricCard
          title="❌ Cancelaciones"
          :value="`${porcentajeCanceladas}%`"
          :class="porcentajeCanceladas > 30 ? 'bg-red-1' : 'bg-green-1'"
        />
      </div>

      <div class="col-12 col-sm-6 col-md-4 col-lg-3">
        <MetricCard title="📈 Variación mensual" :value="variacionMes" />
      </div>
    </div>

    <q-banner
      v-if="!loading && citasFiltradas.length === 0"
      class="bg-orange-2 text-orange-9 q-mb-md"
      rounded
    >
      <q-icon name="info" class="q-mr-sm" />
      No hay citas con los filtros actuales.
    </q-banner>

    <div class="row q-col-gutter-md q-mb-md">
      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">Ventas por día</p>
            <canvas ref="chartPorDia"></canvas>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">Ingresos por servicio</p>
            <canvas ref="chartPorServicio"></canvas>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <q-card class="q-mb-md">
      <q-card-section>
        <p class="text-h6">🏆 Ranking de clientes</p>
        <q-table
          :rows="rankingClientes"
          :columns="columnasRanking"
          row-key="cliente"
          dense
          separator="cell"
          :grid="$q.screen.lt.md"
        >
          <template v-slot:body-cell-total="props">
            <q-td :props="props" class="text-green text-weight-bold"> $ {{ props.value }} </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>

    <q-card>
      <q-card-section class="row items-center justify-between">
        <p class="text-h6">📄 Exportar citas</p>
        <q-btn color="primary" icon="picture_as_pdf" label="Exportar PDF" @click="exportarPDF" />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useQuasar } from 'quasar'
import axios from 'axios'
import { Chart } from 'chart.js/auto'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'

const $q = useQuasar()

const loading = ref(false)
const alerta = ref({ visible: false, mensaje: '', color: 'primary', icono: 'info' })

function mostrarAlerta(m, c = 'primary', i = 'info') {
  alerta.value = { visible: true, mensaje: m, color: c, icono: i }
}

const citas = ref([])
const citasFiltradas = ref([])
const filtroInicio = ref('')
const filtroFin = ref('')

const chartPorDia = ref(null)
const chartPorServicio = ref(null)
let chartDia, chartServicio

function animarValor(target, outputRef, duration = 800) {
  const start = 0
  const startTime = performance.now()

  function animate(time) {
    const progress = Math.min((time - startTime) / duration, 1)
    outputRef.value = Math.round(start + (target - start) * progress)
    if (progress < 1) requestAnimationFrame(animate)
  }
  requestAnimationFrame(animate)
}

const totalCitasAnimado = ref(0)
const ingresosAnimado = ref(0)
const recurrentesAnimado = ref(0)

async function cargarCitas() {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8082/api/citas')
    citas.value = res.data
    citasFiltradas.value = [...citas.value]
    recalcular()
  } catch {
    mostrarAlerta('Error al cargar citas', 'red', 'error')
  } finally {
    loading.value = false
  }
}

function aplicarFiltros() {
  citasFiltradas.value = citas.value.filter((c) => {
    const f = c.fechaHora.split('T')[0]
    return (
      (!filtroInicio.value || f >= filtroInicio.value) && (!filtroFin.value || f <= filtroFin.value)
    )
  })
  mostrarAlerta('Filtros aplicados', 'blue', 'filter_alt')
  recalcular()
}

function limpiarFiltros() {
  filtroInicio.value = ''
  filtroFin.value = ''
  citasFiltradas.value = [...citas.value]
  mostrarAlerta('Filtros limpiados', 'grey', 'clear')
  recalcular()
}

const atendidas = computed(() => citasFiltradas.value.filter((c) => c.estado === 'ATENDIDA'))
const canceladas = computed(() => citasFiltradas.value.filter((c) => c.estado === 'CANCELADA'))

const totalCitas = computed(() => atendidas.value.length)

const totalIngresos = computed(() =>
  atendidas.value.reduce((s, c) => s + c.servicios.reduce((a, b) => a + b.precio, 0), 0),
)

const clientesRecurrentes = computed(() => {
  const map = {}
  atendidas.value.forEach((c) => (map[c.nombreCliente] = (map[c.nombreCliente] || 0) + 1))
  return Object.values(map).filter((v) => v >= 2).length
})

const porcentajeCanceladas = computed(() => {
  if (!citasFiltradas.value.length) return 0
  return ((canceladas.value.length / citasFiltradas.value.length) * 100).toFixed(1)
})

const variacionMes = computed(() => '—')

watch(totalCitas, (v) => animarValor(v, totalCitasAnimado))
watch(totalIngresos, (v) => animarValor(v, ingresosAnimado))
watch(clientesRecurrentes, (v) => animarValor(v, recurrentesAnimado))

const rankingClientes = computed(() => {
  const map = {}
  atendidas.value.forEach((c) => {
    const n = `${c.nombreCliente} ${c.apellidosCliente}`
    const t = c.servicios.reduce((a, b) => a + b.precio, 0)
    if (!map[n]) map[n] = { cliente: n, citas: 0, total: 0 }
    map[n].citas++
    map[n].total += t
  })
  return Object.values(map).sort((a, b) => b.total - a.total)
})

const columnasRanking = [
  { name: 'cliente', label: 'Cliente', field: 'cliente' },
  { name: 'citas', label: 'Citas', field: 'citas', align: 'center' },
  { name: 'total', label: 'Total', field: 'total' },
]

function generarPorDia() {
  const map = {}

  atendidas.value.forEach((c) => {
    const fecha = c.fechaHora.split('T')[0]
    map[fecha] = (map[fecha] || 0) + c.servicios.reduce((a, b) => a + b.precio, 0)
  })

  const ordenado = Object.entries(map).sort((a, b) => new Date(a[0]) - new Date(b[0]))

  const labels = ordenado.map((e) => e[0])
  const data = ordenado.map((e) => e[1])

  if (chartDia) chartDia.destroy()
  chartDia = new Chart(chartPorDia.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [{ data }],
    },
    options: {
      responsive: true,
      scales: {
        x: { ticks: { maxRotation: 45 } },
      },
    },
  })
}

function generarPorServicio() {
  const map = {}
  atendidas.value.forEach((c) =>
    c.servicios.forEach((s) => (map[s.nombre] = (map[s.nombre] || 0) + s.precio)),
  )

  if (chartServicio) chartServicio.destroy()
  chartServicio = new Chart(chartPorServicio.value, {
    type: 'pie',
    data: { labels: Object.keys(map), datasets: [{ data: Object.values(map) }] },
  })
}

function recalcular() {
  generarPorDia()
  generarPorServicio()
}

function exportarPDF() {
  const doc = new jsPDF()
  doc.text('Reporte de Citas – Spa al Instante', 14, 15)

  autoTable(doc, {
    startY: 20,
    head: [['Cliente', 'Estado', 'Total']],
    body: citasFiltradas.value.map((c) => [
      `${c.nombreCliente} ${c.apellidosCliente}`,
      c.estado,
      `$ ${c.servicios.reduce((a, b) => a + b.precio, 0)}`,
    ]),
  })

  doc.save('citas.pdf')
}

onMounted(cargarCitas)
</script>

<style scoped>
.q-page {
  max-width: 1400px;
  margin: auto;
}
</style>
