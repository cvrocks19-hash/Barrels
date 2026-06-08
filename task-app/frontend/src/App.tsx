import { useState, useEffect, useCallback } from 'react';
import Header from './components/Header';
import FilterBar from './components/FilterBar';
import TaskList from './components/TaskList';
import TaskForm from './components/TaskForm';
import { fetchTasks, createTask, updateTask, deleteTask, fetchStats } from './api/tasks';
import type { Task, TaskFilters, TaskStats, CreateTaskInput } from './types/task';

export default function App() {
  const [tasks, setTasks]           = useState<Task[]>([]);
  const [stats, setStats]           = useState<TaskStats>({ total: 0, todo: 0, in_progress: 0, done: 0 });
  const [filters, setFilters]       = useState<TaskFilters>({});
  const [showForm, setShowForm]     = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState<string | null>(null);

  const reload = useCallback(async () => {
    try {
      setLoading(true);
      const [tasksData, statsData] = await Promise.all([fetchTasks(filters), fetchStats()]);
      setTasks(tasksData);
      setStats(statsData);
      setError(null);
    } catch {
      setError('Cannot reach backend. Start the Flask server on port 5001.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { reload(); }, [reload]);

  const handleCreate = async (input: CreateTaskInput) => {
    await createTask(input);
    setShowForm(false);
    reload();
  };

  const handleUpdate = async (id: number, input: Partial<CreateTaskInput>) => {
    await updateTask(id, input);
    setEditingTask(null);
    reload();
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this task?')) return;
    await deleteTask(id);
    reload();
  };

  const handleToggle = async (task: Task) => {
    const next =
      task.status === 'todo'        ? 'in-progress' :
      task.status === 'in-progress' ? 'done'        : 'todo';
    await updateTask(task.id, { status: next });
    reload();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header stats={stats} onNewTask={() => setShowForm(true)} />

      <main className="max-w-2xl mx-auto px-4 py-6 pb-24">
        <FilterBar filters={filters} onChange={setFilters} />

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-16 text-gray-400 text-sm">Loading…</div>
        ) : (
          <TaskList
            tasks={tasks}
            onToggle={handleToggle}
            onEdit={setEditingTask}
            onDelete={handleDelete}
          />
        )}
      </main>

      {(showForm || editingTask) && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask
            ? (input) => handleUpdate(editingTask.id, input)
            : handleCreate
          }
          onClose={() => { setShowForm(false); setEditingTask(null); }}
        />
      )}
    </div>
  );
}
