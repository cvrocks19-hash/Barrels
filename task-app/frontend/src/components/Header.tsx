import type { TaskStats } from '../types/task';

interface Props {
  stats: TaskStats;
  onNewTask: () => void;
}

export default function Header({ stats, onNewTask }: Props) {
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-10 shadow-sm">
      <div className="max-w-2xl mx-auto px-4 py-3 flex items-center justify-between">
        <div>
          <h1 className="text-lg font-bold text-gray-900 leading-tight">Tasks</h1>
          <p className="text-xs text-gray-400">
            {stats.todo} to do
            {stats.in_progress > 0 && ` · ${stats.in_progress} in progress`}
            {stats.done > 0 && ` · ${stats.done} done`}
          </p>
        </div>
        <button
          onClick={onNewTask}
          className="bg-blue-600 text-white text-sm px-4 py-2 rounded-xl font-medium
                     hover:bg-blue-700 active:scale-95 transition-all shadow-sm"
        >
          + New
        </button>
      </div>
    </header>
  );
}
