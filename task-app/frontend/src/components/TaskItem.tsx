import type { Task } from '../types/task';

interface Props {
  task: Task;
  onToggle: (task: Task) => void;
  onEdit:   (task: Task) => void;
  onDelete: (id: number) => void;
}

const PRIORITY_STYLE: Record<string, string> = {
  low:    'bg-green-100 text-green-700',
  medium: 'bg-amber-100 text-amber-700',
  high:   'bg-red-100  text-red-700',
};

const STATUS_ICON: Record<string, string> = {
  'todo':        '○',
  'in-progress': '◑',
  'done':        '●',
};

export default function TaskItem({ task, onToggle, onEdit, onDelete }: Props) {
  const done = task.status === 'done';

  return (
    <div className={`bg-white rounded-xl border border-gray-200 p-4 flex gap-3
                    transition-opacity ${done ? 'opacity-50' : ''}`}>
      {/* Status toggle */}
      <button
        onClick={() => onToggle(task)}
        title="Cycle status: To Do → In Progress → Done"
        className="text-xl mt-0.5 flex-shrink-0 text-blue-500 hover:text-blue-700
                   active:scale-90 transition-transform"
      >
        {STATUS_ICON[task.status]}
      </button>

      {/* Body */}
      <div className="flex-1 min-w-0">
        <p className={`font-medium text-sm leading-snug
                       ${done ? 'line-through text-gray-400' : 'text-gray-900'}`}>
          {task.title}
        </p>
        {task.description && (
          <p className="text-xs text-gray-500 mt-0.5 truncate">{task.description}</p>
        )}
        <div className="flex items-center gap-2 mt-2 flex-wrap">
          <span className={`text-xs px-2 py-0.5 rounded-full font-medium
                            ${PRIORITY_STYLE[task.priority]}`}>
            {task.priority}
          </span>
          {task.category && task.category !== 'general' && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
              {task.category}
            </span>
          )}
          {task.due_date && (
            <span className="text-xs text-gray-400">
              Due {new Date(task.due_date).toLocaleDateString()}
            </span>
          )}
        </div>
      </div>

      {/* Actions */}
      <div className="flex flex-col gap-1 flex-shrink-0">
        <button
          onClick={() => onEdit(task)}
          title="Edit"
          className="text-gray-400 hover:text-gray-700 text-base leading-none px-1"
        >
          ✎
        </button>
        <button
          onClick={() => onDelete(task.id)}
          title="Delete"
          className="text-gray-300 hover:text-red-500 text-base leading-none px-1"
        >
          ✕
        </button>
      </div>
    </div>
  );
}
