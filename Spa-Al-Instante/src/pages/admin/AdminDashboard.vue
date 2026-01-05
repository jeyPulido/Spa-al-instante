<template>
  <q-page class="q-pa-lg bg-grey-2">
    <!-- TÍTULO -->
    <div class="row items-center q-mb-lg">
      <q-icon name="dashboard" size="32px" color="primary" />
      <p class="text-h5 text-weight-bold q-ml-sm">Dashboard Administrativo</p>
    </div>

    <!-- FILTROS -->
    <q-card class="q-mb-lg">
      <q-card-section class="row q-col-gutter-md items-end">
        <div class="col-12 col-md-4">
          <q-input filled v-model="filtroInicio" label="Fecha inicio" type="date" />
        </div>

        <div class="col-12 col-md-4">
          <q-input filled v-model="filtroFin" label="Fecha fin" type="date" />
        </div>

        <div class="col-12 col-md-4">
          <q-btn
            color="primary"
            label="Aplicar filtros"
            icon="filter_alt"
            @click="aplicarFiltros"
            class="full-width"
          />
        </div>
      </q-card-section>
    </q-card>

    <!-- MÉTRICAS -->
    <div class="row q-col-gutter-lg q-mb-lg">
      <div class="col-12 col-md-4">
        <q-card class="bg-primary text-white">
          <q-card-section>
            <p class="text-subtitle1">💰 Ingresos totales</p>
            <p class="text-h5 text-weight-bold">$ {{ totalIngresos }}</p>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-4">
        <q-card>
          <q-card-section>
            <p class="text-subtitle1">📊 Total de citas</p>
            <p class="text-h5 text-weight-bold">{{ citasFiltradas.length }}</p>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-4">
        <q-card>
          <q-card-section>
            <p class="text-subtitle1">📤 Exportar</p>
            <q-btn
              color="green"
              label="Excel"
              icon="table_view"
              class="q-mr-sm"
              @click="exportarExcel"
            />
            <q-btn color="red" label="PDF" icon="picture_as_pdf" @click="exportarPDF" />
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- GRÁFICAS -->
    <div class="row q-col-gutter-lg">
      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">📅 Citas por día</p>
            <canvas ref="chartPorDia"></canvas>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <p class="text-weight-bold">💆 Ingresos por servicio</p>
            <canvas ref="chartPorServicio"></canvas>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Chart } from 'chart.js/auto'
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'

/* ===================== ESTADO ===================== */
const citas = ref([])
const citasFiltradas = ref([])

const filtroInicio = ref('')
const filtroFin = ref('')

const totalIngresos = ref(0)

const chartPorDia = ref(null)
const chartPorServicio = ref(null)

let chartDiaInstance = null
let chartServicioInstance = null

/* ===================== CARGA ===================== */
async function cargarCitas() {
  const res = await axios.get('http://localhost:8082/api/citas')
  citas.value = res.data
  citasFiltradas.value = [...citas.value]
  recalcularTodo()
}

/* ===================== FILTROS ===================== */
function aplicarFiltros() {
  citasFiltradas.value = citas.value.filter((c) => {
    const fecha = c.fechaHora.split('T')[0]
    return (
      (!filtroInicio.value || fecha >= filtroInicio.value) &&
      (!filtroFin.value || fecha <= filtroFin.value)
    )
  })

  recalcularTodo()
}

/* ===================== MÉTRICAS ===================== */
function recalcularTodo() {
  calcularIngresos()
  generarCitasPorDia()
  generarIngresosPorServicio()
}

function calcularIngresos() {
  totalIngresos.value = citasFiltradas.value.reduce((sum, c) => sum + (c.servicio?.precio || 0), 0)
}

/* ===================== GRÁFICAS ===================== */
function generarCitasPorDia() {
  const conteo = {}

  citasFiltradas.value.forEach((c) => {
    const fecha = c.fechaHora.split('T')[0]
    conteo[fecha] = (conteo[fecha] || 0) + 1
  })

  const labels = Object.keys(conteo).sort()
  const data = labels.map((f) => conteo[f])

  if (chartDiaInstance) chartDiaInstance.destroy()

  chartDiaInstance = new Chart(chartPorDia.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [{ data, backgroundColor: '#9c7d60' }],
    },
    options: { plugins: { legend: { display: false } } },
  })
}

function generarIngresosPorServicio() {
  const ingresos = {}

  citasFiltradas.value.forEach((c) => {
    const nombre = c.servicio?.nombre || 'Sin servicio'
    ingresos[nombre] = (ingresos[nombre] || 0) + (c.servicio?.precio || 0)
  })

  const labels = Object.keys(ingresos)
  const data = labels.map((s) => ingresos[s])

  if (chartServicioInstance) chartServicioInstance.destroy()

  chartServicioInstance = new Chart(chartPorServicio.value, {
    type: 'pie',
    data: {
      labels,
      datasets: [{ data }],
    },
  })
}

/* ===================== EXPORTAR ===================== */
function exportarExcel() {
  const data = citasFiltradas.value.map((c) => ({
    Cliente: `${c.nombreCliente} ${c.apellidosCliente}`,
    Servicio: c.servicio?.nombre,
    Precio: c.servicio?.precio,
    Fecha: c.fechaHora,
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Citas')
  XLSX.writeFile(wb, 'reporte_citas.xlsx')
}

function exportarPDF() {
  const doc = new jsPDF()

  doc.text('Reporte de Citas - Spa al Instante', 14, 15)

  autoTable(doc, {
    startY: 25,
    head: [['Cliente', 'Servicio', 'Precio', 'Fecha']],
    body: citasFiltradas.value.map((c) => [
      `${c.nombreCliente} ${c.apellidosCliente}`,
      c.servicio?.nombre,
      `$${c.servicio?.precio}`,
      c.fechaHora,
    ]),
  })

  doc.save('reporte_citas.pdf')
}

onMounted(cargarCitas)
</script>

<style scoped>
.q-page {
  max-width: 1400px;
  margin: auto;
}
</style>
